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