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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TimepointElement;

import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

class TrainInstance {

  private final String trainId;

  private final BasicScheduleElement schedule;

  private final List<Integer> stanoxes = new ArrayList<Integer>();

  private final List<TimepointElement> timepoints = new ArrayList<TimepointElement>();

  private final Map<Integer, Integer> indexByStanox = new HashMap<Integer, Integer>();

  private final long serviceDate;

  private long lastUpdateTime;

  private int lastReportedStanoxIndex = -1;

  private StopTimeUpdate stopTimeUpdate;

  public TrainInstance(String trainId, BasicScheduleElement schedule,
      long serviceDate, long lastUpdateTime) {
    this.trainId = trainId;
    this.schedule = schedule;
    this.serviceDate = serviceDate;
    this.lastUpdateTime = lastUpdateTime;
  }

  public TrainInstance(String trainId, TrainInstance train) {
    this(trainId, train.schedule, train.serviceDate, train.getLastUpdateTime());
    stanoxes.addAll(train.stanoxes);
    timepoints.addAll(train.timepoints);
    indexByStanox.putAll(train.indexByStanox);
  }

  public String getTrainId() {
    return trainId;
  }

  public String getTrainReportingNumber() {
    return trainId.substring(2, 6);
  }

  public BasicScheduleElement getSchedule() {
    return schedule;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void putTimepoint(int stanox, TimepointElement timepoint) {
    indexByStanox.put(stanox, stanoxes.size());
    stanoxes.add(stanox);
    timepoints.add(timepoint);
  }

  public TimepointElement advanceToStanox(int stanox) {
    Integer index = indexByStanox.get(stanox);
    if (index == null) {
      lastReportedStanoxIndex = -1;
      return null;
    }
    lastReportedStanoxIndex = index;
    return timepoints.get(index);
  }

  public boolean hasMoved() {
    return lastReportedStanoxIndex != -1;
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

  public boolean hasMatchingStanoxPrefix(String prefix) {
    if (0 <= lastReportedStanoxIndex
        && lastReportedStanoxIndex < stanoxes.size()
        && hasMatchingPrefix(stanoxes.get(lastReportedStanoxIndex), prefix)) {
      return true;
    }
    if (0 <= lastReportedStanoxIndex + 1
        && lastReportedStanoxIndex + 1 < stanoxes.size()
        && hasMatchingPrefix(stanoxes.get(lastReportedStanoxIndex + 1), prefix)) {
      return true;
    }
    return false;
  }

  private static boolean hasMatchingPrefix(Integer stanox, String prefix) {
    return Integer.toString(stanox).startsWith(prefix);
  }

}