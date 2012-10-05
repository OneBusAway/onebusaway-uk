package org.onebusaway.uk.network_rail.gtfs_realtime;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainDescriberMessage;

public interface TrainDescriberHandler {
  public void handleTrainDescriberMessage(long timestamp, TrainDescriberMessage message, String source);
}
