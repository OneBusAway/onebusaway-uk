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