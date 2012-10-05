package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class LogReplayService {

  private static Logger _log = LoggerFactory.getLogger(LogReplayService.class);

  private MessageHandler _messageHandler;

  @Inject
  public void setMessageHandler(MessageHandler messageHandler) {
    _messageHandler = messageHandler;
  }

  public void parseLogFiles(File base, List<File> files) throws IOException {
    int index = 0;
    for (File file : files) {
      if (index % 1000 == 0) {
        _log.info("files processed=" + index);
      }
      index++;
      String currentPath = file.getAbsolutePath().replace(
          base.getAbsolutePath(), "").substring(1);

      EMessageType messageType = EMessageType.getMessageTypeForFile(file);
      if (messageType == null) {
        _log.warn("could not determine message type for file " + file);
        continue;
      }
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      int lineNumber = 0;
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        try {
          _messageHandler.processMessage(file.lastModified(), messageType,
              line, currentPath);
        } catch (Throwable ex) {
          reader.close();
          throw new IllegalStateException("error processing message: index="
              + index + " file=" + file.getAbsolutePath() + " lineNumber="
              + lineNumber, ex);
        }
      }
      reader.close();
    }
  }
}
