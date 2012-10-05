package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.util.Comparator;

public class EdgeFrequencyComparator implements Comparator<Edge> {

  @Override
  public int compare(Edge a, Edge b) {
    return b.getDurations().size() - a.getDurations().size();
  }
}
