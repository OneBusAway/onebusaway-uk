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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LoggingService {

  private static Logger _log = LoggerFactory.getLogger(LoggingService.class);

  private GtfsRealtimeService _networkRailGtfsRealtimeService;

  private String _logPath;

  /**
   * If true, we will replay recently logged data contained in {@link #_logPath}
   * to rebuild the currently activated train list.
   */
  private boolean _replayLogs = false;

  private int _logHistoryInDays = 10;

  @Inject
  public void setNetworkRailGtfsRealtimeService(
      GtfsRealtimeService networkRailGtfsRealtimeService) {
    _networkRailGtfsRealtimeService = networkRailGtfsRealtimeService;
  }

  public void setLogPath(String logPath) {
    _logPath = logPath;
  }

  public void setReplayLogs(boolean replayLogs) {
    _replayLogs = replayLogs;
  }

  public void replayLogs() throws IOException {
    if (_logPath == null || !_replayLogs) {
      return;
    }
    _log.info("replaying logs");
    File path = new File(_logPath);
    while (path != null && path.getName().contains("%1")) {
      path = path.getParentFile();
    }
    if (path == null || !path.exists()) {
      return;
    }
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -_logHistoryInDays);
    List<File> files = new ArrayList<File>();
    getRecentLogFiles(path, c.getTimeInMillis(), files);
    _log.info("replaying " + files.size() + " log files in " + path);
    for (File file : files) {
      EMessageType messageType = getMessageTypeForFile(file);
      if (messageType == null) {
        _log.warn("could not determine message type for file " + file);
        continue;
      }
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        _networkRailGtfsRealtimeService.processMessages(messageType, line);
      }
    }
    _log.info("replay complete");
  }

  public void logMessage(EMessageType messageType, String jsonMessage) {
    if (_logPath == null) {
      return;
    }

    File path = getNextPath(messageType);
    path.getParentFile().mkdirs();

    BufferedWriter writer = null;
    try {
      writer = new BufferedWriter(new FileWriter(path));
      writer.write(jsonMessage);

    } catch (IOException ex) {
      _log.warn("error writing log file: " + path);
    } finally {
      if (writer != null) {
        try {
          writer.close();
        } catch (IOException e) {
          _log.warn("error closing log file: " + path);
        }
      }
    }
  }

  private File getNextPath(EMessageType messageType) {
    File path = new File(String.format(_logPath, new Date()));
    String name = path.getName();
    int index = name.lastIndexOf('.');
    if (index == -1) {
      name = name + "-" + messageType;
    } else {
      name = name.substring(0, index) + "-" + messageType
          + name.substring(index);
    }
    return new File(path.getParentFile(), name);
  }

  private EMessageType getMessageTypeForFile(File file) {
    String name = file.getName();
    for (EMessageType messageType : EMessageType.values()) {
      if (name.contains(messageType.toString())) {
        return messageType;
      }
    }
    return null;
  }

  private void getRecentLogFiles(File logDir, long minLastModified,
      List<File> matchingFiles) {
    if (logDir.isDirectory()) {
      for (File childDir : logDir.listFiles()) {
        getRecentLogFiles(childDir, minLastModified, matchingFiles);
      }
    } else if (logDir.getName().endsWith(".json")
        && logDir.lastModified() > minLastModified) {
      matchingFiles.add(logDir);
    }
  }
}
