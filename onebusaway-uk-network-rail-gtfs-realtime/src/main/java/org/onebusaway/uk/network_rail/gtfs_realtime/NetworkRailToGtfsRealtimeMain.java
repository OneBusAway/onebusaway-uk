/**
 * Copyright (C) 2012 Google, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.cli.Daemonizer;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.onebusaway.status_exporter.StatusServletSource;
import org.onebusway.gtfs_realtime.exporter.TripUpdatesFileWriter;
import org.onebusway.gtfs_realtime.exporter.TripUpdatesServlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class NetworkRailToGtfsRealtimeMain {

  private static final String ARG_USERNAME = "username";

  private static final String ARG_PASSWORD = "password";

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_BERTH_MAPPING_PATH = "berthMappingPath";

  private static final String ARG_LOG_PATH = "logPath";

  private static final String ARG_REPLAY_LOGS = "replayLogs";

  private static final String ARG_STATE_PATH = "statePath";

  private static final String ARG_NARRATIVE_PATH = "narrativePath";

  private static final String ARG_TRIP_UPDATES_PATH = "tripUpdatesPath";

  private static final String ARG_TRIP_UPDATES_URL = "tripUpdatesUrl";

  private MessageListenerService _messageListenerService;

  private GtfsRealtimeService _gtfsRealtimeService;

  private LoggingService _loggingService;

  private NarrativeService _narrativeService;

  private LifecycleService _lifecycleService;

  public static void main(String[] args) throws Exception {
    NetworkRailToGtfsRealtimeMain m = new NetworkRailToGtfsRealtimeMain();
    m.run(args);
  }

  @Inject
  public void setMessageListenerService(
      MessageListenerService messageListenerService) {
    _messageListenerService = messageListenerService;
  }

  @Inject
  public void setGtfsRealtimeService(GtfsRealtimeService gtfsRealtimeService) {
    _gtfsRealtimeService = gtfsRealtimeService;
  }

  @Inject
  public void setLoggingService(LoggingService loggingService) {
    _loggingService = loggingService;
  }

  @Inject
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  @Inject
  public void setStatusServletSource(StatusServletSource statusServletSource) {
    // Noop, just here to make sure that status servlet is instantiated.
  }

  @Inject
  public void setLifecycleService(LifecycleService lifecycleService) {
    _lifecycleService = lifecycleService;
  }

  public void run(String[] args) throws Exception {

    Options options = new Options();
    buildOptions(options);
    Daemonizer.buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    Daemonizer.handleDaemonization(cli);

    Set<Module> modules = new HashSet<Module>();
    NetworkRailGtfsRealtimeModule.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    _messageListenerService.setUsername(cli.getOptionValue(ARG_USERNAME));
    _messageListenerService.setPassword(cli.getOptionValue(ARG_PASSWORD));

    _gtfsRealtimeService.setAtocTimetablePath(new File(
        cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));
    if (cli.hasOption(ARG_BERTH_MAPPING_PATH)) {
      _gtfsRealtimeService.setBerthMappingPath(new File(
          cli.getOptionValue(ARG_BERTH_MAPPING_PATH)));
    }
    if (cli.hasOption(ARG_LOG_PATH)) {
      _loggingService.setLogPath(cli.getOptionValue(ARG_LOG_PATH));
    }
    _loggingService.setReplayLogs(cli.hasOption(ARG_REPLAY_LOGS));

    if (cli.hasOption(ARG_STATE_PATH)) {
      _gtfsRealtimeService.setStatePath(new File(
          cli.getOptionValue(ARG_STATE_PATH)));
    }
    
    if(cli.hasOption(ARG_NARRATIVE_PATH)) {
      _narrativeService.setLogPath(cli.getOptionValue(ARG_NARRATIVE_PATH));
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
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_USERNAME, true, "user name");
    options.addOption(ARG_PASSWORD, true, "password");
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_BERTH_MAPPING_PATH, true, "berth listing path");
    options.addOption(ARG_LOG_PATH, true, "log path");
    options.addOption(ARG_REPLAY_LOGS, false, "replay log");
    options.addOption(ARG_STATE_PATH, true, "state path");
    options.addOption(ARG_NARRATIVE_PATH, true, "narrative path");
    options.addOption(ARG_TRIP_UPDATES_PATH, true, "trip updates path");
    options.addOption(ARG_TRIP_UPDATES_URL, true, "trip updates url");
  }
}
