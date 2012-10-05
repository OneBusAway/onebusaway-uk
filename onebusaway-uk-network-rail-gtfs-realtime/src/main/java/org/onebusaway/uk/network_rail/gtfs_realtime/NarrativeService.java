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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainInstance;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthMessage;

@Singleton
public class NarrativeService {

  private static final DateFormat _format = DateFormat.getTimeInstance(DateFormat.SHORT);

  private Map<String, File> _pathsByTrainId = new HashMap<String, File>();

  private Map<String, File> _pathsByTrainReportingNumber = new HashMap<String, File>();

  private String _logPath = null;

  private String _currentFile;
  
  public void setLogPath(String logPath) {
    _logPath = logPath;
  }

  @PreDestroy
  public void stop() {
    _pathsByTrainId.clear();
    _pathsByTrainReportingNumber.clear();
  }

  public void setCurrentFile(String currentFile) {
    _currentFile = currentFile;
  }

  public void addMessage(BerthMessage berthMessage, String message) {
    addMessage(berthMessage.getDescr(), berthMessage.getTimeAsLong(), message);
  }

  public void addMessage(String trainReportingNumber, long timestamp,
      String message) {
    if (_logPath == null) {
      return;
    }
    Date time = new Date(timestamp);
    PrintWriter writer = getWriter(trainReportingNumber, time,
        _pathsByTrainReportingNumber);
    writer.println(_format.format(time) + " - " + _currentFile + " - "
        + message);
    writer.flush();
  }

  public void addMessage(TrainInstance instance, String message) {
    if (_logPath == null) {
      return;
    }
    Date time = new Date(instance.getLastUpdateTime());
    PrintWriter writer = getWriter(instance.getTrainId(), time, _pathsByTrainId);
    writer.println(_format.format(time) + " - " + _currentFile + " - "
        + message);
    writer.close();
  }

  public void closeMessages(TrainInstance instance) {
    _pathsByTrainId.remove(instance.getTrainId());
  }

  private PrintWriter getWriter(String id, Date time, Map<String, File> writers) {
    File path = writers.get(id);
    if (path == null) {
      path = new File(String.format(_logPath, time, id));
      path.getParentFile().mkdirs();
      writers.put(id, path);
    }
    try {
      return new PrintWriter(new FileWriter(path, true));
    } catch (IOException ex) {
      throw new IllegalStateException("error opening path " + path, ex);
    }
  }
}
