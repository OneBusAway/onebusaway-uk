package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.util.HashMap;
import java.util.Map;

import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TimepointElement;

import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

class TrainInstance {

  private final String trainId;

  private final BasicScheduleElement schedule;

  private final Map<Integer, TimepointElement> timepointsByStanox = new HashMap<Integer, TimepointElement>();

  private long lastUpdateTime;

  private StopTimeUpdate stopTimeUpdate;

  public TrainInstance(String trainId, BasicScheduleElement schedule,
      long lastUpdateTime) {
    this.trainId = trainId;
    this.schedule = schedule;
    this.lastUpdateTime = lastUpdateTime;
  }

  public TrainInstance(String trainId, TrainInstance train) {
    this(trainId, train.schedule, train.getLastUpdateTime());
    timepointsByStanox.putAll(train.timepointsByStanox);
  }

  public String getTrainId() {
    return trainId;
  }

  public BasicScheduleElement getSchedule() {
    return schedule;
  }

  public void putTimepoint(int stanox, TimepointElement timepoint) {
    timepointsByStanox.put(stanox, timepoint);
  }

  public TimepointElement getTimepointForStanox(int stanox) {
    return timepointsByStanox.get(stanox);
  }

  public void setLastUpdateTime(long time) {
    lastUpdateTime = time;
  }

  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setStopTimeUpdate(StopTimeUpdate stopTimeUpdate) {
    this.stopTimeUpdate = stopTimeUpdate;
  }

  public StopTimeUpdate getStopTimeUpdate() {
    return stopTimeUpdate;
  }
}