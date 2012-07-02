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

import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.activemq.transport.stomp.Stomp.Headers.Subscribe;
import org.apache.activemq.transport.stomp.StompConnection;
import org.apache.activemq.transport.stomp.StompFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class MessageListenerService {

  private static final Logger _log = LoggerFactory.getLogger(MessageListenerService.class);

  private static final String TOPIC_TRAIN_MVT = "/topic/TRAIN_MVT_ALL_TOC";

  private static final String TOPIC_TD = "/topic/TD_ALL_SIG_AREA";

  private GtfsRealtimeService _gtfsRealtimeService;

  private LoggingService _loggingService;

  private StompConnection _connection = new StompConnection();

  private String _host = "datafeeds.networkrail.co.uk";

  private int _port = 61618;

  private String _username;

  private String _password;

  private ExecutorService _executor;

  private Future<?> _task;

  private boolean _enabled = false;

  @Inject
  public void setGtfsRealtimeService(GtfsRealtimeService gtfsRealtimeService) {
    _gtfsRealtimeService = gtfsRealtimeService;
  }

  @Inject
  public void setLoggingService(LoggingService loggingService) {
    _loggingService = loggingService;
  }

  public void setUsername(String username) {
    _username = username;
  }

  public void setPassword(String password) {
    _password = password;
  }

  public void setEnabled(boolean enabled) {
    _enabled = enabled;
  }

  @PostConstruct
  public void start() throws Exception {
    if (!_enabled) {
      _log.info("stop connection disabled");
      return;
    }
    _log.info("opening stop connection");
    _connection.open(_host, _port);
    _connection.connect(_username, _password);

    _connection.subscribe(TOPIC_TRAIN_MVT, Subscribe.AckModeValues.CLIENT);
    _connection.subscribe(TOPIC_TD, Subscribe.AckModeValues.CLIENT);

    _connection.begin("tx2");

    _executor = Executors.newSingleThreadExecutor();
    _task = _executor.submit(new MessageProcessor());
    _log.info("connection open");
  }

  @PreDestroy
  public void stop() {

    if (_task != null) {
      _task.cancel(true);
      _task = null;
    }

    if (_executor != null) {
      _executor.shutdownNow();
      _executor = null;
    }

    if (_connection != null) {
      try {
        _connection.disconnect();
        _connection = null;
      } catch (Exception ex) {
        _log.error("error disconnecting", ex);
      }
    }
  }

  private void processNextMessage() throws Exception {
    StompFrame message = null;
    try {
      message = _connection.receive();
    } catch (SocketTimeoutException ex) {
      _log.warn("timeout");
      return;
    }

    EMessageType messageType = getMessageTypeForMessage(message);
    String body = message.getBody();

    _connection.ack(message, "tx2");

    _loggingService.logMessage(messageType, body);
    _gtfsRealtimeService.processMessages(messageType, body);

  }

  private EMessageType getMessageTypeForMessage(StompFrame frame) {
    Map<String, String> headers = frame.getHeaders();
    String destination = headers.get("destination");
    if (destination == null) {
      _log.warn("message without destination header");
      return null;
    }
    if (destination.equals(TOPIC_TRAIN_MVT)) {
      return EMessageType.TRAIN_MOVEMENT;
    } else if (destination.equals(TOPIC_TD)) {
      return EMessageType.TD;
    } else {
      _log.info("unknown destination=" + destination);
      return null;
    }
  }

  private class MessageProcessor implements Runnable {

    @Override
    public void run() {

      while (!Thread.interrupted()) {
        try {
          processNextMessage();
        } catch (Exception ex) {
          _log.error("error proccessing message", ex);
        }
      }
    }

  }
}
