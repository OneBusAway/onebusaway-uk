package org.onebusaway.uk.network_rail.gtfs_realtime.model;

import java.io.Serializable;

public final class TrainState implements Serializable {

  private static final long serialVersionUID = 1L;

  private String trainId;

  private String trainUid;

  private long lastUpdateTimestamp;

  public String getTrainId() {
    return trainId;
  }

  public void setTrainId(String trainId) {
    this.trainId = trainId;
  }

  public String getTrainUid() {
    return trainUid;
  }

  public void setTrainUid(String trainUid) {
    this.trainUid = trainUid;
  }

  public long getLastUpdateTimestamp() {
    return lastUpdateTimestamp;
  }

  public void setLastUpdateTimestamp(long lastLocalUpdateTimestamp) {
    this.lastUpdateTimestamp = lastLocalUpdateTimestamp;
  }
}
