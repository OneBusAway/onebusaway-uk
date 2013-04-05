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
package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.io.Serializable;
import java.util.List;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrackIdentifier;

public class SerializedRawEdge implements Serializable {

  private static final long serialVersionUID = 2L;

  private final TrackIdentifier from;

  private final TrackIdentifier to;
  
  private final List<Integer> durations;

  public SerializedRawEdge(TrackIdentifier from, TrackIdentifier to, List<Integer> durations) {
    this.from = from;
    this.to = to;
    this.durations = durations;
  }

  public TrackIdentifier getFrom() {
    return from;
  }

  public TrackIdentifier getTo() {
    return to;
  }

  public List<Integer> getDurations() {
    return durations;
  }
}
