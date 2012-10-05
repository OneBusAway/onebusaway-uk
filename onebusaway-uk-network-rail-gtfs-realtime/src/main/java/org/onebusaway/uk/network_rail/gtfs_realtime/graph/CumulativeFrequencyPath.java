package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


class CumulativeFrequencyPath {

  public static CumulativeFrequencyNode getMostFrequentPathBetweenNodes(
      RawBerthNode from, RawBerthNode to, int suggestedDuration) {

    Deque<CumulativeFrequencyNode> queue = new ArrayDeque<CumulativeFrequencyNode>();
    queue.add(new CumulativeFrequencyNode(from));

    int maxDuration = suggestedDuration;
    double bestFrequency = 0;
    CumulativeFrequencyNode bestPath = null;

    while (!queue.isEmpty()) {
      CumulativeFrequencyNode frequencyNode = queue.poll();

      if (frequencyNode.getTotalDuration() > maxDuration * 1.25) {
        continue;
      }
      if (frequencyNode.getFrequency() < bestFrequency / 2) {
        continue;
      }
//      System.out.println("  " + frequencyNode + " frequency="
//          + frequencyNode.getFrequency() + " duration="
//          + frequencyNode.getTotalDuration() + " segmentLength="
//          + frequencyNode.getSegmentLength());
      if (frequencyNode.getNode() == to) {
        double frequency = frequencyNode.getFrequency();
        if (frequency > bestFrequency) {
          bestPath = frequencyNode;
          bestFrequency = frequency;
          maxDuration = frequencyNode.getTotalDuration();
//          System.out.println("    new best");
        }
      } else {
        queue.addAll(frequencyNode.getOutgoing());
      }
    }

    return bestPath;
  }

  static class CumulativeFrequencyNode {
    private final RawBerthNode node;
    private final CumulativeFrequencyNode parent;
    private final int segmentLength;
    private final int totalFrequency;
    private final int totalDuration;
    private Set<RawBerthNode> visited;

    public CumulativeFrequencyNode(RawBerthNode node) {
      this(node, null, 0, 0, 0, new HashSet<RawBerthNode>());
    }

    private CumulativeFrequencyNode(RawBerthNode node, CumulativeFrequencyNode parent,
        int segmentLength, int totalFrequency, int totalDuration,
        Set<RawBerthNode> visited) {
      this.node = node;
      this.parent = parent;
      this.segmentLength = segmentLength;
      this.totalFrequency = totalFrequency;
      this.totalDuration = totalDuration;
      this.visited = visited;
    }

    public List<CumulativeFrequencyNode> getOutgoing() {
      List<CumulativeFrequencyNode> outgoing = new ArrayList<CumulativeFrequencyNode>();
      for (Edge edge : node.getEdges()) {
        if (visited.contains(edge.getTo())) {
          continue;
        }
        int duration = Math.max(edge.getAverageDuration(), 5);
        Set<RawBerthNode> nextVisited = new HashSet<RawBerthNode>(visited);
        nextVisited.add(edge.getTo());
        CumulativeFrequencyNode frequencyNode = new CumulativeFrequencyNode(
            edge.getTo(), this, segmentLength + 1, totalFrequency
                + edge.getDurations().size(), totalDuration + duration,
            nextVisited);
        outgoing.add(frequencyNode);
      }
      return outgoing;
    }

    public RawBerthNode getNode() {
      return node;
    }

    public CumulativeFrequencyNode getParent() {
      return parent;
    }

    public int getSegmentLength() {
      return segmentLength;
    }

    public int getTotalDuration() {
      return totalDuration;
    }

    public double getFrequency() {
      if (segmentLength == 0) {
        return Double.POSITIVE_INFINITY;
      }
      return (double) totalFrequency / segmentLength;
    }

    @Override
    public String toString() {
      StringBuilder b = new StringBuilder();
      if (parent != null) {
        b.append(parent.toString());
        b.append(", ");
      }
      b.append(node.toString());
      b.append("=");
      b.append(totalDuration);
      return b.toString();
    }
  }
}