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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.onebusway.gtfs_realtime.exporter.TripUpdatesFileWriter;
import org.onebusway.gtfs_realtime.exporter.TripUpdatesServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class ReplayMain {

  private static final Logger _log = LoggerFactory.getLogger(ReplayMain.class);

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_NAPTAN_PATH = "naptanPath";

  private static final String ARG_TRIP_UPDATES_PATH = "tripUpdatesPath";

  private static final String ARG_TRIP_UPDATES_URL = "tripUpdatesUrl";

  private NetworkRailGtfsRealtimeService _service;

  private LifecycleService _lifecycleService;

  public static void main(String[] args) throws Exception {
    ReplayMain m = new ReplayMain();
    m.run(args);
  }

  @Inject
  public void setService(NetworkRailGtfsRealtimeService service) {
    _service = service;
  }

  @Inject
  public void setLifecycleService(LifecycleService lifecycleService) {
    _lifecycleService = lifecycleService;
  }

  private void run(String[] args) throws IOException, ParseException,
      JAXBException {
    Options options = new Options();
    buildOptions(options);
    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    Set<Module> modules = new HashSet<Module>();
    NetworkRailGtfsRealtimeModule.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    _service.setAtocTimetablePath(new File(
        cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));

    if (cli.hasOption(ARG_NAPTAN_PATH)) {
      _service.setNaptanPath(new File(cli.getOptionValue(ARG_NAPTAN_PATH)));
    }

    if (cli.hasOption(ARG_TRIP_UPDATES_PATH)) {
      TripUpdatesFileWriter writer = injector.getInstance(TripUpdatesFileWriter.class);
      writer.setPath(new File(cli.getOptionValue(ARG_TRIP_UPDATES_PATH)));
    }
    if (cli.hasOption(ARG_TRIP_UPDATES_URL)) {
      TripUpdatesServlet servlet = injector.getInstance(TripUpdatesServlet.class);
      servlet.setUrl(new URL(cli.getOptionValue(ARG_TRIP_UPDATES_URL)));
    }

    _lifecycleService.start();

    List<File> files = new ArrayList<File>();
    for (String path : cli.getArgs()) {
      collectFiles(new File(path), files);
    }

    for (File file : files) {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("[")) {
          continue;
        }
        _service.processMessages(line);
      }
      reader.close();
    }

    _log.info("all done");
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_NAPTAN_PATH, true, "naptan path");
    options.addOption(ARG_TRIP_UPDATES_PATH, true, "trip updates path");
    options.addOption(ARG_TRIP_UPDATES_URL, true, "trip updates url");
  }

  private List<File> collectFiles(File path, List<File> matches) {
    if (path.isDirectory()) {
      for (File child : path.listFiles()) {
        collectFiles(child, matches);
      }
    } else if (path.getName().endsWith(".json")) {
      matches.add(path);
    }
    return matches;
  }
}
