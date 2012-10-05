/**
 * Copyright (C) 2012 Google, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.uk.network_rail.gtfs_realtime.instance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TimepointElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.SerializedNarrative;

import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;

public class TrainInstance {

  private static final DateFormat _format = new SimpleDateFormat(
      "yyyy-MM-dd HH-mm-ss");

  private final String trainId;

  private final long serviceDate;

  private long lastUpdateTime;

  private BasicScheduleElement schedule;

  private int scheduleDeviation = 0;

  private List<SerializedNarrative.Event> events = new ArrayList<SerializedNarrative.Event>();

  private long lastStanoxTime;

  private int lastStanox;

  private final Map<Integer, List<TimepointElement>> timepointsByStanox = new HashMap<Integer, List<TimepointElement>>();

  private StopTimeUpdate stopTimeUpdate;

  public TrainInstance(String trainId, long serviceDate) {
    this.trainId = trainId;
    this.serviceDate = serviceDate;
  }

  public String getTrainId() {
    return trainId;
  }

  public String getTrainReportingNumber() {
    return trainId.substring(2, 6);
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public void setSchedule(BasicScheduleElement schedule) {
    this.schedule = schedule;
    this.timepointsByStanox.clear();
  }

  public BasicScheduleElement getSchedule() {
    return schedule;
  }

  public int getScheduleDeviation() {
    return scheduleDeviation;
  }

  public void setScheduleDeviation(int scheduleDeviation) {
    this.scheduleDeviation = scheduleDeviation;
  }

  public List<SerializedNarrative.Event> getEvents() {
    return events;
  }

  public void addEvent(SerializedNarrative.Event event) {
    events.add(event);
  }

  public long getLastStanoxTime() {
    return lastStanoxTime;
  }

  public int getLastStanox() {
    return lastStanox;
  }

  public void setLastStanox(long time, int stanox) {
    this.lastStanoxTime = time;
    this.lastStanox = stanox;
  }

  public void putTimepoint(int stanox, TimepointElement timepoint) {
    List<TimepointElement> timepoints = timepointsByStanox.get(stanox);
    if (timepoints == null) {
      timepoints = new ArrayList<TimepointElement>();
      timepointsByStanox.put(stanox, timepoints);
    }
    timepoints.add(timepoint);
  }

  public List<TimepointElement> getTimepointForStanox(int stanox) {
    List<TimepointElement> timepoints = timepointsByStanox.get(stanox);
    if (timepoints == null) {
      return Collections.emptyList();
    }
    return timepoints;
  }

  public String getDescription() {
    return trainId + " "
        + _format.format(new Date(events.get(0).getTimestamp()));
  }

  public void setStopTimeUpdate(StopTimeUpdate stopTimeUpdate) {
    this.stopTimeUpdate = stopTimeUpdate;
  }

  public StopTimeUpdate getStopTimeUpdate() {
    return stopTimeUpdate;
  }

}