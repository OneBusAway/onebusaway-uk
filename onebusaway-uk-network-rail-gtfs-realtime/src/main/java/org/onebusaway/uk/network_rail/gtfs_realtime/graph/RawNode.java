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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrackIdentifier;

public abstract class RawNode {

  public abstract TrackIdentifier getId();

  public abstract void getSerializedEdges(List<SerializedRawEdge> edges);

  @Override
  public String toString() {
    return getId().toString();
  }

  protected <T> void addEdge(Map<T, List<Integer>> edges, T node, int duration) {
    List<Integer> durations = edges.get(node);
    if (durations == null) {
      durations = new ArrayList<Integer>();
      edges.put(node, durations);
    }
    durations.add(duration);
  }

  public static int average(List<Integer> durations) {
    int total = 0;
    for (int duration : durations) {
      total += duration;
    }
    return total / durations.size();
  }

  protected <T extends RawNode> void getSerializedEdges(
      Map<T, List<Integer>> edges, List<SerializedRawEdge> serializedEdges,
      boolean forward) {
    for (Map.Entry<T, List<Integer>> entry : edges.entrySet()) {
      TrackIdentifier from = forward ? getId() : entry.getKey().getId();
      TrackIdentifier to = forward ? entry.getKey().getId() : getId();
      SerializedRawEdge edge = new SerializedRawEdge(from, to, entry.getValue());
      serializedEdges.add(edge);
    }
  }
}