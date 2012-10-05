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
