/**
 * Copyright (C) 2012 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.collections.Range;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.FileMatcher;
import org.onebusaway.uk.network_rail.gtfs_realtime.NetworkRailGtfsRealtimeModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.TimetableService;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthStepIdentifier;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.SerializedNarrative;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrackIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ConstructBerthToStanoxGraphMain {

  private static final Logger _log = LoggerFactory.getLogger(ConstructBerthToStanoxGraphMain.class);

  private static final String ARG_NARRATIVE_PATH = "narrativePath";

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_BERTH_TO_STANOX_AREA_MAPPING_PATH = "berthToStanoxAreaMappingPath";

  private static final String ARG_GRAPH_PATH = "graphPath";

  private static final String ARG_EDGE_LOG_PATH = "edgeLogPath";

  private static DateFormat _timeFormat = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm");

  public static void main(String[] args) throws ParseException, IOException {
    ConstructBerthToStanoxGraphMain m = new ConstructBerthToStanoxGraphMain();
    m.run(args);
  }

  private TimetableService _timetableService;

  private LifecycleService _lifecycleService;

  private RawGraph _graph = new RawGraph();

  private String _edgeLogPath;

  private BufferedWriter _edgeLog;

  @Inject
  public void setTimetableService(TimetableService timetableService) {
    _timetableService = timetableService;
  }

  @Inject
  public void setLifecycleService(LifecycleService lifecycleService) {
    _lifecycleService = lifecycleService;
  }

  private void run(String[] args) throws ParseException, IOException {
    Options options = new Options();
    buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    Set<Module> modules = new HashSet<Module>();
    NetworkRailGtfsRealtimeModule.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    _timetableService.readScheduleData(new File(
        cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));
    // _timetableService.readBerthToStanoxAreaMapping(new File(
    // cli.getOptionValue(ARG_BERTH_TO_STANOX_AREA_MAPPING_PATH)));

    _edgeLogPath = cli.getOptionValue(ARG_EDGE_LOG_PATH);

    _lifecycleService.start();

    File narrativePath = new File(cli.getOptionValue(ARG_NARRATIVE_PATH));
    FileMatcher matcher = new FileMatcher();
    matcher.setExtension(".pb");
    List<File> narrativeFiles = matcher.matchFiles(narrativePath);
    _log.info("files=" + narrativeFiles.size());

    _edgeLog = new BufferedWriter(new FileWriter(_edgeLogPath));

    int i = 0;
    for (File narrativeFile : narrativeFiles) {
      if (i % 1000 == 0) {
        _log.info("files=" + i);
      }
      i++;
      InputStream in = new BufferedInputStream(new FileInputStream(
          narrativeFile));
      SerializedNarrative.TrainInstance trainInstance = SerializedNarrative.TrainInstance.parseFrom(in);
      expandGraph(trainInstance);
    }
    _edgeLog.close();

    _graph.write(new File(cli.getOptionValue(ARG_GRAPH_PATH)));
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_NARRATIVE_PATH, true, "log path");
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_BERTH_TO_STANOX_AREA_MAPPING_PATH, true,
        "berth to stanox mapping path");
    options.addOption(ARG_GRAPH_PATH, true, "combined graph path");
    options.addOption(ARG_EDGE_LOG_PATH, true, "edge log path");
  }

  private void expandGraph(SerializedNarrative.TrainInstance trainInstance)
      throws IOException {

    List<SerializedNarrative.Event> events = trainInstance.getEventList();

    if (!mostlyAutomatic(events)) {
      return;
    }

    if (hasHighVelocity(events)) {
      return;
    }

    addEdges(trainInstance, events);
  }

  private void addEdges(SerializedNarrative.TrainInstance trainInstance,
      List<SerializedNarrative.Event> events) throws IOException {

    TrackIdentifier prevId = null;
    long prevTime = 0;
    BerthStepIdentifier prevBerthStepId = null;
    long prevBerthStepTime = 0;

    for (SerializedNarrative.Event event : events) {
      TrackIdentifier id = getTrackIdentifierForEvent(event);
      if (id != null) {
        if (prevId != null) {
          int duration = (int) ((event.getTimestamp() - prevTime) / 1000);
          _graph.addEdge(prevId, id, duration);
          logEdge(prevId, id, trainInstance);
        }
        if (prevBerthStepId != null && prevBerthStepId != prevId
            && id instanceof BerthStepIdentifier) {
          int duration = (int) ((event.getTimestamp() - prevBerthStepTime) / 1000);
          _graph.addEdge(prevBerthStepId, id, duration);
          logEdge(prevBerthStepId, id, trainInstance);
        }
        prevId = id;
        prevTime = event.getTimestamp();
        if (id instanceof BerthStepIdentifier) {
          prevBerthStepId = (BerthStepIdentifier) id;
          prevBerthStepTime = event.getTimestamp();
        }
      }
    }
  }

  private TrackIdentifier getTrackIdentifierForEvent(
      SerializedNarrative.Event event) {
    if (event.hasTrainMovement()) {
      SerializedNarrative.TrainMovementEvent trainMovement = event.getTrainMovement();
      return TrackIdentifier.getStanoxIdentifier(trainMovement.getStanoxId());
    } else if (event.hasBerthStep()) {
      SerializedNarrative.BerthStepEvent berthStep = event.getBerthStep();
      return TrackIdentifier.getBerthIdentifier(berthStep.getFromBerthId(),
          berthStep.getToBerthId());
    } else {
      return null;
    }
  }

  private void logEdge(TrackIdentifier prevId, TrackIdentifier id,
      SerializedNarrative.TrainInstance trainInstance) throws IOException {
    String line = prevId
        + " "
        + id
        + " "
        + trainInstance.getTrainReportingNumber()
        + " "
        + trainInstance.getTrainId()
        + " "
        + trainInstance.getTrainUid()
        + " "
        + _timeFormat.format(new Date(trainInstance.getEvent(0).getTimestamp()))
        + "\n";
    _edgeLog.write(line);
  }

  private boolean hasHighVelocity(List<SerializedNarrative.Event> events) {
    SerializedNarrative.Event prevEvent = null;
    int prevStanox = -1;

    for (SerializedNarrative.Event event : events) {
      if (!event.hasTrainMovement()) {
        continue;
      }
      SerializedNarrative.TrainMovementEvent movement = event.getTrainMovement();
      if (!movement.hasType()
          || movement.getType() != SerializedNarrative.TrainMovementEvent.Type.MOVEMENT) {
        continue;
      }
      int stanox = movement.getStanoxId();
      if (prevStanox != -1) {
        double range = computeRangeForStanox(prevStanox, stanox);
        double time = (event.getTimestamp() - prevEvent.getTimestamp()) / 1000;
        if (time == 0) {
          if (range > 2000) {
            // System.out.println("range=" + range);
            // printLocationsForStanox(prevStanox);
            // System.out.println();
            // printLocationsForStanox(stanox);
            // System.out.println();
          }
        } else {
          double vel = range / time;
          double limit = time < 30 ? 200 : (time < 60 ? 100 : (time < 120 ? 80
              : 60));
          if (vel > limit) {
            return true;
            // System.out.println("vel=" + vel);
            // printLocationsForStanox(prevStanox);
            // System.out.println();
            // printLocationsForStanox(stanox);
            // System.out.println();
          }
        }
      }
      prevStanox = stanox;
      prevEvent = event;
    }
    return false;
  }

  private double computeRangeForStanox(int idA, int idB) {
    Range x = new Range();
    Range y = new Range();
    updateRangeForStanox(idA, x, y);
    updateRangeForStanox(idB, x, y);
    if (x.isEmpty()) {
      return 0;
    }
    double dx = x.getRange();
    double dy = y.getRange();
    return Math.sqrt(dx * dx + dy * dy);
  }

  private void updateRangeForStanox(int stanox, Range x, Range y) {
    Set<String> tiplocs = _timetableService.getTiplocsForStanox(stanox);
    for (String tiploc : tiplocs) {
      StationElement station = _timetableService.getStationForTiploc(tiploc);
      if (station == null) {
        continue;
      }
      x.addValue(station.getEasting());
      y.addValue(station.getNorthing());
    }
  }

  private boolean mostlyAutomatic(List<SerializedNarrative.Event> events) {
    int total = 0;
    int automatic = 0;
    for (SerializedNarrative.Event event : events) {
      if (event.hasTrainMovement()) {
        SerializedNarrative.TrainMovementEvent trainMovement = event.getTrainMovement();
        if (trainMovement.hasEventSource()
            && trainMovement.getEventSource().equals("AUTOMATIC")) {
          automatic++;
        }
        total++;
      }
    }
    return (double) automatic / total > 0.5;
  }
}
