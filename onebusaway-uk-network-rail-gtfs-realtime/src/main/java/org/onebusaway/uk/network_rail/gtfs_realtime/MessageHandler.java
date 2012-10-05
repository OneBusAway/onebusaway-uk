package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.IOException;

public interface MessageHandler {
  public void processMessage(long timestamp, EMessageType messageType, String jsonMessage, String source)
      throws IOException;
}
