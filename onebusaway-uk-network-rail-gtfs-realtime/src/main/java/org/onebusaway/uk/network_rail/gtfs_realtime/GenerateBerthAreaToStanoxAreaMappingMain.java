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

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GenerateBerthAreaToStanoxAreaMappingMain {

  private static Logger _log = LoggerFactory.getLogger(GenerateBerthAreaToStanoxAreaMappingMain.class);

  private static final String ARG_BERTH_MAPPING_PATH = "berthMappingBath";

  private static final String ARG_OUTPUT_PATH = "outputPath";

  public static void main(String[] args) throws ParseException, IOException {
    GenerateBerthAreaToStanoxAreaMappingMain m = new GenerateBerthAreaToStanoxAreaMappingMain();
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

    Map<String, Set<String>> berthAreaToStanoxAreas = new HashMap<String, Set<String>>();

    BufferedReader reader = new BufferedReader(new FileReader(
        cli.getOptionValue(ARG_BERTH_MAPPING_PATH)));
    String line = null;

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(",");
      String area = tokens[0];
      String stanoxValue = tokens[4];
      if (stanoxValue.length() < 2) {
        _log.info("stanox without area: " + stanoxValue);
        continue;
      }
      String stanoxArea = stanoxValue.substring(0, 2);
      Set<String> stanoxAreas = berthAreaToStanoxAreas.get(area);
      if (stanoxAreas == null) {
        stanoxAreas = new HashSet<String>();
        berthAreaToStanoxAreas.put(area, stanoxAreas);
      }
      stanoxAreas.add(stanoxArea);
    }

    reader.close();

    List<String> berthAreas = new ArrayList<String>(
        berthAreaToStanoxAreas.keySet());
    Collections.sort(berthAreas);

    OutputStream out = cli.hasOption(ARG_OUTPUT_PATH) ? new FileOutputStream(
        cli.getOptionValue(ARG_OUTPUT_PATH)) : System.out;
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
    writer.println("berth_area,stanox_area");

    for (String berthArea : berthAreas) {
      List<String> stanoxAreas = new ArrayList<String>(
          berthAreaToStanoxAreas.get(berthArea));
      Collections.sort(stanoxAreas);
      for (String stanoxArea : stanoxAreas) {
        writer.println(berthArea + "," + stanoxArea);
      }
    }

    writer.close();
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_OUTPUT_PATH, true, "output path");
    options.addOption(ARG_BERTH_MAPPING_PATH, true, "berth mapping path");
  }
}
