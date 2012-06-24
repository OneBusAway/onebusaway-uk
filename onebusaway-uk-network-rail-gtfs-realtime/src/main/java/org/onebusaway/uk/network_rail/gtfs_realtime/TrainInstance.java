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