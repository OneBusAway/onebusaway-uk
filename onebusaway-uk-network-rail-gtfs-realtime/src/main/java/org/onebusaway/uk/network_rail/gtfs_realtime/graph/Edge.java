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

import java.util.List;


class Edge {
  private final RawBerthNode from;
  private final RawBerthNode to;
  private final List<Integer> durations;
  private final int averageDuration;

  public Edge(RawBerthNode from, RawBerthNode to, List<Integer> durations, int averageDuration) {
    this.from = from;
    this.to = to;
    this.durations = durations;
    this.averageDuration = averageDuration;
  }

  public RawBerthNode getFrom() {
    return from;
  }

  public RawBerthNode getTo() {
    return to;
  }
  
  public int getFrequency() {
    return durations.size();
  }

  public List<Integer> getDurations() {
    return durations;
  }

  public int getAverageDuration() {
    return averageDuration;
  }

  @Override
  public String toString() {
    return from + " " + to + " " + averageDuration + "[" + durations.size()
        + "]";
  }
}