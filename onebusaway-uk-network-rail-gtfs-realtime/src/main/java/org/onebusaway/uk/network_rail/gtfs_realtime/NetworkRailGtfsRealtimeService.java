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
package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.atoc.timetable_parser.TimetableBundle;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TimepointElement;
import org.onebusaway.uk.network_rail.cif.TiplocInsertElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.Body;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.Message;
import org.onebusaway.uk.parser.DefaultContentHandler;
import org.onebusaway.uk.parser.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Processor {

  private static final Logger _log = LoggerFactory.getLogger(Processor.class);

  private Gson _gson;

  private Map<String, StationElement> _stationsByTiploc = new HashMap<String, StationElement>();

  private Map<String, TiplocInsertElement> _insertsByTiploc = new HashMap<String, TiplocInsertElement>();

  private Map<String, BasicScheduleElement> _schedulesByTrainUid = new HashMap<String, BasicScheduleElement>();

  private Map<String, TrainInstance> _trainsById = new HashMap<String, Processor.TrainInstance>();

  private File _atocTimetablePath;

  private long _messageCount;

  private long _unknownTrainUidCount;

  private long _unknownTrainIdCount;

  private long _emptyLocStanoxCount;

  private long _unknownStanoxCount;

  public Processor() {
    _gson = new GsonBuilder().setFieldNamingPolicy(
        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  }

  public void setAtocTimetablePath(File atocTimetablePath) {
    _atocTimetablePath = atocTimetablePath;
  }

  public void start() throws IOException {

    TimetableBundle bundle = new TimetableBundle(_atocTimetablePath);
    _log.info("loading master station names");
    bundle.readMasterStationNames(new MasterStationNameHandler());
    _log.info("loading timetable");
    bundle.readTimetable(new ScheduleHandler());
    _log.info("load complete");
  }

  public void processMessages(String jsonMessage) {
    Message[] messages = _gson.fromJson(jsonMessage, Message[].class);
    for (Message message : messages) {
      processMessage(message);
    }
  }

  private void processMessage(Message message) {
    _messageCount++;
    int msgType = Integer.parseInt(message.getHeader().getMsgType());
    switch (msgType) {
      case 1:
        handleActivation(message);
        break;
      case 3:
        handleMovement(message);
        break;
    }

    if (_messageCount % 100 == 0) {
      printStats();
    }
  }

  private void printStats() {
    System.out.println("========================================");
    System.out.println("       messages=" + _messageCount);
    System.out.println("unknownTrainUid=" + _unknownTrainUidCount);
    System.out.println(" unknownTrainId=" + _unknownTrainIdCount);
    System.out.println(" emptyLocStanox=" + _emptyLocStanoxCount);
    System.out.println("  unknownStanox=" + _unknownStanoxCount);
  }

  private void handleActivation(Message message) {
    Body body = message.getBody();
    BasicScheduleElement schedule = getBestScheduleForTrainUid(body.getTrainUid());
    if (schedule == null) {
      return;
    }
    String trainId = body.getTrainId();
    TrainInstance instance = new TrainInstance(schedule);

    for (TimepointElement timepoint : schedule.getTimepoints()) {
      TiplocInsertElement insert = _insertsByTiploc.get(timepoint.getTiploc());
      if (insert == null) {
        continue;
      }
      instance.putTimepoint(insert.getStanox(), timepoint);
    }

    _trainsById.put(trainId, instance);
  }

  private void handleMovement(Message message) {
    Body body = message.getBody();
    String trainId = body.getTrainId();
    TrainInstance instance = _trainsById.get(trainId);
    if (instance == null) {
      _unknownTrainIdCount++;
      return;
    }
    if (body.getLocStanox() == null) {
      _emptyLocStanoxCount++;
    }
    int stanox = Integer.parseInt(body.getLocStanox());
    TimepointElement timepoint = instance.getTimepointForStanox(stanox);
    if (timepoint == null) {
      _unknownStanoxCount++;
    }
  }

  private BasicScheduleElement getBestScheduleForTrainUid(String trainUid) {
    BasicScheduleElement schedule = _schedulesByTrainUid.get(trainUid);
    if (schedule == null) {
      _unknownTrainUidCount++;
    }
    return schedule;
  }

  private class MasterStationNameHandler extends DefaultContentHandler {

    @Override
    public void startElement(Element element) {
      if (element instanceof StationElement) {
        StationElement station = (StationElement) element;
        StationElement existing = _stationsByTiploc.put(station.getTiploc(),
            station);
        if (existing != null) {
          _log.warn("duplicate station tiploc=" + station.getTiploc());
        }
      }
    }
  }

  private class ScheduleHandler extends DefaultContentHandler {

    @Override
    public void endElement(Element element) {
      if (element instanceof TiplocInsertElement) {
        TiplocInsertElement insert = (TiplocInsertElement) element;
        TiplocInsertElement existing = _insertsByTiploc.put(insert.getTiploc(),
            insert);
        if (existing != null) {
          _log.warn("duplicate insert tiploc=" + insert.getTiploc());
        }
      }
      if (element instanceof BasicScheduleElement) {
        BasicScheduleElement schedule = (BasicScheduleElement) element;
        if (!_schedulesByTrainUid.containsKey(schedule.getTrainUid())) {
          _schedulesByTrainUid.put(schedule.getTrainUid(), schedule);
        }
      }
    }
  }

  private class TrainInstance {

    private final Map<Integer, TimepointElement> timepointsByStanox = new HashMap<Integer, TimepointElement>();

    public TrainInstance(BasicScheduleElement schedule) {

    }

    public void putTimepoint(int stanox, TimepointElement timepoint) {
      timepointsByStanox.put(stanox, timepoint);
    }

    public TimepointElement getTimepointForStanox(int stanox) {
      return timepointsByStanox.get(stanox);
    }
  }
}
