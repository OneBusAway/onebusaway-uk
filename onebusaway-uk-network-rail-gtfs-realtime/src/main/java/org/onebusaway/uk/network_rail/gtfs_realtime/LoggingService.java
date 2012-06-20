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

  private NetworkRailGtfsRealtimeService _networkRailGtfsRealtimeService;

  private String _logPath;

  /**
   * If true, we will replay recently logged data contained in {@link #_logPath}
   * to rebuild the currently activated train list.
   */
  private boolean _replayLogs = false;

  @Inject
  public void setNetworkRailGtfsRealtimeService(
      NetworkRailGtfsRealtimeService networkRailGtfsRealtimeService) {
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
    File path = new File(_logPath);
    while (path != null && path.getName().contains("%1")) {
      path = path.getParentFile();
    }
    if (path == null || !path.exists()) {
      return;
    }
    Calendar c = Calendar.getInstance();
    c.add(Calendar.DAY_OF_MONTH, -1);
    List<File> files = new ArrayList<File>();
    getRecentLogFiles(path, c.getTimeInMillis(), files);
    _log.info("replaying " + files.size() + " log files in " + path);
    for (File file : files) {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        _networkRailGtfsRealtimeService.processMessages(line);
      }
    }
  }

  public void logMessage(String jsonMessage) {
    if (_logPath == null) {
      return;
    }

    File path = new File(String.format(_logPath, new Date()));
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
