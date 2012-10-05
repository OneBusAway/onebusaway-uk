package org.onebusaway.uk.network_rail.gtfs_realtime.model;

import org.onebusaway.uk.network_rail.gtfs_realtime.ETrainMovementMessageType;

public class TrainMovementEvent extends Event {

  private TrainMovementMessage message;

  public TrainMovementEvent(long timestamp, String source,
      TrainMovementMessage message) {
    super(timestamp, source);
    this.message = message;
  }

  public TrainMovementMessage getMessage() {
    return message;
  }

  @Override
  public TrackIdentifier getTrackIdentifier() {
    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));
    if (msgType == ETrainMovementMessageType.MOVEMENT
        && message.getBody().getLocStanox() != null) {
      int stanox = Integer.parseInt(message.getBody().getLocStanox());
      if (stanox != 0) {
        return TrackIdentifier.getStanoxIdentifier(stanox);
      }
    }
    return null;
  }

  @Override
  public String toLogString() {
    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));
    StringBuilder b = new StringBuilder();
    b.append(super.toLogString());
    b.append(" tm ");
    b.append(message.getBody().getTrainId());    
    b.append(" ");
    b.append(msgType.name());
    
    if (msgType == ETrainMovementMessageType.ACTIVATION) {
      b.append(" ");
      b.append(message.getBody().getTrainUid());
    }
    else if (msgType == ETrainMovementMessageType.MOVEMENT) {
      b.append(" ");
      b.append(message.getBody().getLocStanox());
      b.append(" ");
      b.append(message.getBody().getEventSource());
    }
    return b.toString();
  }

  @Override
  public String toString() {
    return toLogString();
  }
}
