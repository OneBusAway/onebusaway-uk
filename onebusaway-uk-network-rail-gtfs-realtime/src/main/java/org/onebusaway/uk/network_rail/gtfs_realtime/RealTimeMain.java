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
import java.net.SocketTimeoutException;

import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealTimeMain {

  private static final Logger _log = LoggerFactory.getLogger(RealTimeMain.class);

  private static final String ARG_USERNAME = "userName";

  private static final String ARG_PASSWORD = "password";

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private Processor _processor = new Processor();

  StompConnection _connection = new StompConnection();

  public static void main(String[] args) throws ParseException {
    RealTimeMain m = new RealTimeMain();
    m.run(args);
  }

  public void run(String[] args) throws ParseException {

    Options options = new Options();
    buildOptions(options);
    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    try {
      _processor.setAtocTimetablePath(new File(
          cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));
      _processor.start();

      connect(cli);

      proccessData();

    } catch (Exception ex) {
      _log.error("error processing", ex);
    }

    if (_connection != null) {
      try {
        _connection.disconnect();
      } catch (Exception ex) {
        _log.error("error disconnecting", ex);
      }
    }
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_USERNAME, true, "user name");
    options.addOption(ARG_PASSWORD, true, "password");
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
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

      _connection.ack(message, "tx2");

      _processor.processMessages(message.getBody());
    }
  }
}
