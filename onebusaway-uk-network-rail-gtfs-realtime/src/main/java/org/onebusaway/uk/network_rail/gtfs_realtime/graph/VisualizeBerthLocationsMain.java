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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.collections.Max;
import org.onebusaway.collections.Range;
import org.onebusaway.collections.tuple.Pair;
import org.onebusaway.collections.tuple.Tuples;
import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.NetworkRailGtfsRealtimeModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.TimetableService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class VisualizeBerthLocationsMain {

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_BERTH_MAPPING_PATH = "berthMappingBath";

  private static final String ARG_OUTPUT_PATH = "outputPath";

  public static void main(String[] args) throws ParseException, IOException {
    VisualizeBerthLocationsMain m = new VisualizeBerthLocationsMain();
    m.run(args);
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

    TimetableService model = new TimetableService();
    model.readScheduleData(new File(cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));

    Map<String, String> regionToStyle = new HashMap<String, String>();

    PrintWriter writer = new PrintWriter(new File(
        cli.getOptionValue(ARG_OUTPUT_PATH)));
    writer.println("area,style,from,to,region,stanox,tiploc,lat,lon,name");

    BufferedReader reader = new BufferedReader(new FileReader(
        cli.getOptionValue(ARG_BERTH_MAPPING_PATH)));
    String line = null;
    
    Map<String,Pair<Range>> rangesByArea = new HashMap<String, Pair<Range>>();

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(",");
      String area = tokens[0];
      String from = tokens[2];
      String to = tokens[3];
      String stanoxValue = tokens[4];
      if (stanoxValue.length() < 2) {
        System.out.println(stanoxValue);
        continue;
      }
      int stanox = Integer.parseInt(stanoxValue);
      String region = stanoxValue.substring(0, 2);

      String style = regionToStyle.get(area);

      if (style == null) {
        style = IconStyles.SMALL[regionToStyle.size() % IconStyles.SMALL.length];
        regionToStyle.put(area, style);
      }
      
      Pair<Range> ranges = rangesByArea.get(area);
      if (ranges == null) {
        ranges = Tuples.pair(new Range(), new Range());
        rangesByArea.put(area, ranges);
      }
      Range xRange = ranges.getFirst();
      Range yRange = ranges.getSecond();
        
      for (String tiploc : model.getTiplocsForStanox(stanox)) {
        StationElement station = model.getStationForTiploc(tiploc);
        if (station != null) {
          writer.println(area + "," + style + "," + from + "," + to + ","
              + region + "," + stanox + "," + tiploc + "," + station.getLat()
              + "," + station.getLon() + "," + station.getName());
          xRange.addValue(station.getEasting());
          yRange.addValue(station.getNorthing());
        }
      }
    }
    
    Max<String> maxRange = new Max<String>();
    for (Map.Entry<String,Pair<Range>> entry : rangesByArea.entrySet()) {
      String area = entry.getKey();
      Pair<Range> ranges = entry.getValue();
      if (ranges.getFirst().isEmpty() ) {
        continue;
      }
      double dx = ranges.getFirst().getRange();
      double dy = ranges.getFirst().getRange();
      double v = Math.sqrt(dx*dx + dy*dy);
      maxRange.add(v, area);
    }
    
    System.out.println(maxRange.getMaxElement() + " " + maxRange.getMaxValue());

    reader.close();

    writer.close();
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_OUTPUT_PATH, true, "output path");
    options.addOption(ARG_BERTH_MAPPING_PATH, true, "berth mapping path");
  }
}
