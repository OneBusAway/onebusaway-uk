package org.onebusaway.uk.network_rail.gtfs_realtime;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainMovementMessage;

public interface TrainMovementHandler {
  public void handleTrainMovementMessage(long timestamp, TrainMovementMessage message, String source);
}
