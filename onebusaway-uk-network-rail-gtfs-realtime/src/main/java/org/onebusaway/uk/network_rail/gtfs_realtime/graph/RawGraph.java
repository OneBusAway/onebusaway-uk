package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthStepIdentifier;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.StanoxIdentifier;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrackIdentifier;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class RawGraph {

  private Map<TrackIdentifier, RawNode> _nodesByIdentifier = new HashMap<TrackIdentifier, RawNode>();

  public void addEdge(TrackIdentifier from, TrackIdentifier to, int duration) {

    RawNode fromNode = getOrCreateNode(from);
    RawNode toNode = getOrCreateNode(to);

    if (fromNode instanceof RawBerthNode) {
      RawBerthNode fromBerthNode = (RawBerthNode) fromNode;
      if (toNode instanceof RawBerthNode) {
        RawBerthNode toBerthNode = (RawBerthNode) toNode;
        fromBerthNode.addOutgoing(toBerthNode, duration);
      }
      if (toNode instanceof RawStanoxNode) {
        RawStanoxNode toStanoxNode = (RawStanoxNode) toNode;
        fromBerthNode.addStanox(toStanoxNode);
      }
    }
    if (fromNode instanceof RawStanoxNode) {
      RawStanoxNode fromStanoxNode = (RawStanoxNode) fromNode;
      fromStanoxNode.addOutgoing(toNode, duration);
      if (toNode instanceof RawBerthNode) {
        RawBerthNode toBerthNode = (RawBerthNode) toNode;
        toBerthNode.addStanox(fromStanoxNode);
      }
    }
    if (toNode instanceof RawStanoxNode) {
      RawStanoxNode toStanoxNode = (RawStanoxNode) toNode;
      toStanoxNode.addIncoming(fromNode, duration);
    }
  }

  public Collection<RawNode> getNodes() {
    return _nodesByIdentifier.values();
  }

  public Iterable<RawBerthNode> getBerthNodes() {
    return Iterables.filter(_nodesByIdentifier.values(), RawBerthNode.class);
  }

  public Iterable<RawStanoxNode> getStanoxNodes() {
    return Iterables.filter(_nodesByIdentifier.values(), RawStanoxNode.class);
  }

  public RawNode getNode(TrackIdentifier id) {
    return _nodesByIdentifier.get(id);
  }

  public static class BerthPath {
    public List<RawBerthNode> nodes;
    public double distance;

    public BerthPath(List<RawBerthNode> path, double distance) {
      this.nodes = path;
      this.distance = distance;
    }
  }

  public BerthPath getShortestPath(RawBerthNode nodeFrom, RawBerthNode nodeTo) {
    return getShortestPath(nodeFrom, nodeTo, Double.POSITIVE_INFINITY);
  }

  public BerthPath getShortestPath(RawBerthNode nodeFrom, RawBerthNode nodeTo,
      double maxTime) {
    Queue<OrderedRawBerthNode> queue = new PriorityQueue<OrderedRawBerthNode>();
    queue.add(new OrderedRawBerthNode(nodeFrom, null, 0.0));

    Map<RawBerthNode, RawBerthNode> parents = new HashMap<RawBerthNode, RawBerthNode>();
    Set<RawBerthNode> visited = new HashSet<RawBerthNode>();

    while (!queue.isEmpty()) {
      OrderedRawBerthNode currentNode = queue.poll();
      RawBerthNode node = currentNode.getNode();
      if (!visited.add(node)) {
        continue;
      }
      if (currentNode.getDistance() > maxTime) {
        break;
      }
      parents.put(node, currentNode.getParent());
      if (node == nodeTo) {
        List<RawBerthNode> path = new ArrayList<RawBerthNode>();
        RawBerthNode last = node;
        while (last != null) {
          path.add(last);
          last = parents.get(last);
        }
        Collections.reverse(path);
        return new BerthPath(path, currentNode.getDistance());
      }

      for (Map.Entry<RawBerthNode, List<Integer>> entry : node.getOutgoing().entrySet()) {
        RawBerthNode outgoing = entry.getKey();
        int avgDuration = RawNode.average(entry.getValue());
        queue.add(new OrderedRawBerthNode(outgoing, node,
            currentNode.getDistance() + avgDuration));
      }
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public void read(File file) throws IOException {
    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
        new FileInputStream(file)));
    List<SerializedRawEdge> edges = null;
    try {
      edges = (List<SerializedRawEdge>) in.readObject();
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    } finally {
      in.close();
    }

    for (SerializedRawEdge edge : edges) {
      for (int duration : edge.getDurations()) {
        addEdge(edge.getFrom(), edge.getTo(), duration);
      }
    }
  }

  public void write(File file) throws IOException {
    List<SerializedRawEdge> edges = new ArrayList<SerializedRawEdge>();
    for (RawNode node : _nodesByIdentifier.values()) {
      node.getSerializedEdges(edges);
    }
    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
        new FileOutputStream(file)));
    out.writeObject(edges);
    out.close();
  }

  private RawNode getOrCreateNode(TrackIdentifier id) {
    RawNode node = _nodesByIdentifier.get(id);
    if (node == null) {
      if (id instanceof StanoxIdentifier) {
        node = new RawStanoxNode((StanoxIdentifier) id);
      } else {
        node = new RawBerthNode((BerthStepIdentifier) id);
      }
      _nodesByIdentifier.put(id, node);
    }
    return node;
  }
}
