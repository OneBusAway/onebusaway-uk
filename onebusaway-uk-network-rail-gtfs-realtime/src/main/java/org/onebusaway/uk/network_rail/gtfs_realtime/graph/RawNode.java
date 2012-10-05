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