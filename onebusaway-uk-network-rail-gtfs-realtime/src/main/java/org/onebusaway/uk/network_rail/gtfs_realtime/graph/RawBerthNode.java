package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthStepIdentifier;

public class RawBerthNode extends RawNode {

  private final BerthStepIdentifier id;

  private Map<RawBerthNode, List<Integer>> _outgoing = new HashMap<RawBerthNode, List<Integer>>();

  private Set<RawStanoxNode> _stanox = new HashSet<RawStanoxNode>();

  public RawBerthNode(BerthStepIdentifier id) {
    this.id = id;
  }

  public BerthStepIdentifier getId() {
    return id;
  }

  /**
   * See {@link BerthStepIdentifier#isDirectlyLinked(BerthStepIdentifier)}.
   * 
   * @param node
   * @return
   */
  public boolean isDirectlyLinked(RawBerthNode node) {
    return id.isDirectlyLinked(node.id);
  }

  public Map<RawBerthNode, List<Integer>> getOutgoing() {
    return _outgoing;
  }

  public List<Edge> getEdges() {
    List<Edge> edges = new ArrayList<Edge>();
    for (Map.Entry<RawBerthNode, List<Integer>> entry : _outgoing.entrySet()) {
      List<Integer> durations = entry.getValue();
      edges.add(new Edge(this, entry.getKey(), durations, average(durations)));
    }
    return edges;
  }

  public List<Integer> getOutgoingDurations(RawBerthNode node) {
    return _outgoing.get(node);
  }

  public int getOutgoingAverageDuration(RawBerthNode node) {
    List<Integer> durations = _outgoing.get(node);
    if (durations == null) {
      return 0;
    }
    return average(durations);
  }

  public void addOutgoing(RawBerthNode node, int duration) {
    List<Integer> durations = _outgoing.get(node);
    if (durations == null) {
      durations = new ArrayList<Integer>();
      _outgoing.put(node, durations);
    }
    durations.add(duration);
  }

  public void removeOutgoing(RawBerthNode to) {
    _outgoing.remove(to);
  }

  public void setOutgoing(RawBerthNode nodeTo, List<Integer> durations) {
    _outgoing.put(nodeTo, durations);
  }

  public Set<RawStanoxNode> getStanox() {
    return _stanox;
  }

  public void addStanox(RawStanoxNode node) {
    _stanox.add(node);
  }

  @Override
  public void getSerializedEdges(List<SerializedRawEdge> serializedEdges) {
    getSerializedEdges(_outgoing, serializedEdges, true);
  }
}