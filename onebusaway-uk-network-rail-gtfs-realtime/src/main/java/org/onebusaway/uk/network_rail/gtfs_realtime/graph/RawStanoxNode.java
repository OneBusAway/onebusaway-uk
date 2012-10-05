package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.StanoxIdentifier;

public class RawStanoxNode extends RawNode {

  private final StanoxIdentifier id;

  private Map<RawNode, List<Integer>> _incoming = new HashMap<RawNode, List<Integer>>();

  private Map<RawNode, List<Integer>> _outgoing = new HashMap<RawNode, List<Integer>>();

  public RawStanoxNode(StanoxIdentifier id) {
    this.id = id;
  }

  @Override
  public StanoxIdentifier getId() {
    return id;
  }

  public Map<RawNode, List<Integer>> getIncoming() {
    return _incoming;
  }

  public Map<RawNode, List<Integer>> getOutgoing() {
    return _outgoing;
  }

  public Set<RawBerthNode> getBerthConnections() {
    Set<RawBerthNode> connections = new HashSet<RawBerthNode>();
    addConnections(_incoming.keySet(), connections);
    addConnections(_outgoing.keySet(), connections);
    return connections;
  }

  private void addConnections(Set<RawNode> nodes, Set<RawBerthNode> connections) {
    for (RawNode node : nodes) {
      if (node instanceof RawBerthNode) {
        connections.add((RawBerthNode) node);
      }
    }
  }

  public void addIncoming(RawNode node, int duration) {
    addEdge(_incoming, node, duration);
  }

  public void addOutgoing(RawNode node, int duration) {
    addEdge(_outgoing, node, duration);
  }

  public void removeIncoming(RawNode to) {
    _incoming.remove(to);
  }

  public void removeOutgoing(RawNode to) {
    _outgoing.remove(to);
  }

  public void setOutgoing(RawStanoxNode nodeTo, List<Integer> durations) {
    _outgoing.put(nodeTo, durations);
  }

  @Override
  public void getSerializedEdges(List<SerializedRawEdge> serializedEdges) {
    getSerializedEdges(_incoming, serializedEdges, false);
    getSerializedEdges(_outgoing, serializedEdges, true);
  }
}