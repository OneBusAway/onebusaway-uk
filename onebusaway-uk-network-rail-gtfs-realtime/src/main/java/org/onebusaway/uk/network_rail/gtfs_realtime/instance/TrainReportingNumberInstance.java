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
package org.onebusaway.uk.network_rail.gtfs_realtime.instance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.SerializedNarrative;

public class TrainReportingNumberInstance {

  private final String _trainReportingNumber;

  private long _lastUpdateTime = 0;

  private Map<String, TrainInstance> _trainsById = new HashMap<String, TrainInstance>();

  private List<SerializedNarrative.Event> _events = new ArrayList<SerializedNarrative.Event>();

  private List<SerializedNarrative.Event> _unmatchedEvents = new ArrayList<SerializedNarrative.Event>();

  public TrainReportingNumberInstance(String trainReportingNumber) {
    _trainReportingNumber = trainReportingNumber;
  }
  
  public String getTrainReportingNumber() {
    return _trainReportingNumber;
  }

  public long getLastUpdateTime() {
    return _lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    _lastUpdateTime = lastUpdateTime;
  }

  public TrainInstance getTrainInstanceForTrainId(String trainId) {
    TrainInstance instance = _trainsById.get(trainId);
    if (instance == null) {
      Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
      c.setTimeInMillis(_lastUpdateTime);
      c.set(Calendar.HOUR_OF_DAY, 0);
      c.set(Calendar.MINUTE, 0);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      long serviceDate = c.getTimeInMillis();

      instance = new TrainInstance(trainId, serviceDate);
      _trainsById.put(trainId, instance);
    }
    return instance;
  }

  public Collection<TrainInstance> getAllTrainInstances() {
    return _trainsById.values();
  }

  public void addEvent(SerializedNarrative.Event event) {
    _events.add(event);
  }
  
  public List<SerializedNarrative.Event> getEvents() {
    return _events;
  }

  public void addUnmatchedEvent(SerializedNarrative.Event event) {
    _unmatchedEvents.add(event);
  }
}
