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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
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

public class RealTimeMain {

  private static final Logger _log = LoggerFactory.getLogger(RealTimeMain.class);

  private static final String ARG_USERNAME = "userName";

  private static final String ARG_PASSWORD = "password";

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_LOG_PATH = "logPath";

  private static final String ARG_TRIP_UPDATES_PATH = "tripUpdatesPath";

  private static final String ARG_TRIP_UPDATES_URL = "tripUpdatesUrl";

  private NetworkRailGtfsRealtimeService _service;

  private LifecycleService _lifecycleService;

  private StompConnection _connection = new StompConnection();

  private String _logPath = null;

  public static void main(String[] args) throws ParseException, IOException {
    RealTimeMain m = new RealTimeMain();
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

  public void run(String[] args) throws ParseException, IOException {

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

    if (cli.hasOption(ARG_LOG_PATH)) {
      _logPath = cli.getOptionValue(ARG_LOG_PATH);
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

    try {
      connect(cli);
      proccessData();
    } catch (Exception ex) {
      _log.error("error processing", ex);
    }

    disconnect();
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_USERNAME, true, "user name");
    options.addOption(ARG_PASSWORD, true, "password");
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_LOG_PATH, true, "log path");
    options.addOption(ARG_TRIP_UPDATES_PATH, true, "trip updates path");
    options.addOption(ARG_TRIP_UPDATES_URL, true, "trip updates url");
  }

  private void connect(CommandLine cli) throws Exception {

    _connection.open("datafeeds.networkrail.co.uk", 61618);
    _connection.connect(cli.getOptionValue(ARG_USERNAME),
        cli.getOptionValue(ARG_PASSWORD));

    _connection.subscribe("/topic/TRAIN_MVT_ALL_TOC",
        Subscribe.AckModeValues.CLIENT);
    _connection.begin("tx2");
  }

  private void proccessData() throws Exception, IOException {
    while (true) {
      StompFrame message = null;
      try {
        message = _connection.receive();
      } catch (SocketTimeoutException ex) {
        _log.warn("timeout");
        continue;
      }

      String body = message.getBody();

      _connection.ack(message, "tx2");

      // Is there a better
      if (body.startsWith("[")) {
        if (_logPath != null) {
          File path = new File(String.format(_logPath, new Date()));
          path.getParentFile().mkdirs();
          BufferedWriter writer = new BufferedWriter(new FileWriter(path));
          writer.write(body);
          writer.close();
        }

        _service.processMessages(body);
      }
    }
  }

  private void disconnect() {
    if (_connection != null) {
      try {
        _connection.disconnect();
        _connection = null;
      } catch (Exception ex) {
        _log.error("error disconnecting", ex);
      }
    }
  }

}
