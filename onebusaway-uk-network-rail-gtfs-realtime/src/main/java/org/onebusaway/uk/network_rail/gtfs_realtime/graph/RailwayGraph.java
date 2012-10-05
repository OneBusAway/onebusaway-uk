package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.onebusaway.collections.Min;
import org.onebusaway.collections.Range;
import org.onebusaway.uk.parser.ProjectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.strtree.STRtree;

public class RailwayGraph {

  private static final Logger _log = LoggerFactory.getLogger(RailwayGraph.class);

  private Map<Long, Node> _nodesById = new HashMap<Long, Node>();

  private STRtree _index = null;

  public Collection<Node> getNodes() {
    return _nodesById.values();
  }

  public void addNode(long id, double lat, double lon) {
    Node node = new Node(id, lat, lon);
    Node existingNode = _nodesById.put(id, node);
    if (existingNode != null) {
      throw new IllegalStateException("duplicate node: " + node);
    }
  }

  public void addEdge(long fromId, long toId) {
    Node fromNode = getNode(fromId);
    Node toNode = getNode(toId);
    fromNode.edges.add(toNode);
    toNode.edges.add(fromNode);
  }

  public Node getClosestNode(double x, double y) {
    if (_index == null) {
      _index = new STRtree(_nodesById.size());
      for (Node node : _nodesById.values()) {
        _index.insert(new Envelope(node.x, node.x, node.y, node.y), node);
      }
      _index.build();
    }
    for (double r = 10; r <= 1000; r *= 2) {
      Envelope env = new Envelope(x - r, x + r, y - r, y + r);
      @SuppressWarnings({"unchecked"})
      List<Node> query = _index.query(env);
      Min<Node> best = new Min<Node>();
      for (Node node : query) {
        best.add(node.getDistance(x, y), node);
      }
      if (!best.isEmpty()) {
        return best.getMinElement();
      }
    }
    return null;
  }

  public RailwayPath getPath(Node from, Node to) {
    double maxDistance = from.getDistance(to) * 2;
    Set<Node> visited = new HashSet<Node>();
    PriorityQueue<QueueNode> queue = new PriorityQueue<QueueNode>();
    queue.add(new QueueNode(from, null, 0, 0));
    Map<Node, Node> parents = new HashMap<Node, Node>();
    while (!queue.isEmpty()) {
      QueueNode current = queue.poll();
      Node currentNode = current.node;
      visited.add(currentNode);
      parents.put(currentNode, current.parent);
      if (currentNode == to) {
        List<Node> result = new ArrayList<Node>();
        while (currentNode != null) {
          result.add(currentNode);
          currentNode = parents.get(currentNode);
        }
        Collections.reverse(result);
        RailwayPath path = new RailwayPath();
        path.nodes = result;
        path.distance = current.score;
        return path;
      }

      if (current.score > maxDistance) {
        break;
      }

      for (Node edge : currentNode.edges) {
        if (visited.contains(edge)) {
          continue;
        }
        double tentativeScore = current.score + currentNode.getDistance(edge);
        double heuristicScore = tentativeScore + edge.getDistance(to);
        queue.add(new QueueNode(edge, currentNode, tentativeScore,
            heuristicScore));
      }
    }

    return null;
  }

  public void pruneIslandNodes() {
    long totalNodes = 0;
    long prunedNodes = 0;
    for (Iterator<Node> it = _nodesById.values().iterator(); it.hasNext();) {
      Node node = it.next();
      if (node.edges.isEmpty()) {
        it.remove();
        prunedNodes++;
      }
      totalNodes++;
    }
    _log.info("pruned nodes=" + prunedNodes + "/" + totalNodes);
  }

  public void pruneClusters(double minClusterSize) {
    Set<Node> remainingNodes = new HashSet<Node>();
    remainingNodes.addAll(_nodesById.values());
    while (!remainingNodes.isEmpty()) {
      Node first = remainingNodes.iterator().next();
      Set<Node> cluster = exploreCluster(first, remainingNodes);

      Range xRange = new Range();
      Range yRange = new Range();
      for (Node node : cluster) {
        xRange.addValue(node.x);
        yRange.addValue(node.y);
      }
      double dx = xRange.getRange();
      double dy = yRange.getRange();
      double d = Math.sqrt(dx * dx + dy * dy);
      if (d < minClusterSize) {
        _log.info("pruning cluster: nodes=" + cluster.size() + " distance=" + d);
        for (Node node : cluster) {
          _nodesById.remove(node.id);
        }
      }
    }
  }

  private Set<Node> exploreCluster(Node first, Set<Node> remainingNodes) {
    Deque<Node> queue = new ArrayDeque<Node>();
    queue.add(first);
    Set<Node> cluster = new HashSet<Node>();
    while (!queue.isEmpty()) {
      Node node = queue.pop();
      if (!remainingNodes.remove(node)) {
        continue;
      }
      cluster.add(node);
      for (Node edge : node.edges) {
        queue.add(edge);
      }
    }
    return cluster;
  }

  private Node getNode(long id) {
    Node node = _nodesById.get(id);
    if (node == null) {
      throw new IllegalStateException("unknown node=" + id);
    }
    return node;
  }

  public static class Node {

    private final long id;

    private final double lat;

    private final double lon;

    private double x;

    private double y;

    private final Set<Node> edges = new HashSet<Node>();

    private Node(long id, double lat, double lon) {
      this.id = id;
      this.lat = lat;
      this.lon = lon;
      Point2D.Double point = ProjectionSupport.convertFromLatLon(lat, lon);
      this.x = point.x;
      this.y = point.y;
    }

    public double getLat() {
      return lat;
    }

    public double getLon() {
      return lon;
    }

    public double getX() {
      return x;
    }

    public void setX(double x) {
      this.x = x;
    }

    public double getY() {
      return y;
    }

    public void setY(double y) {
      this.y = y;
    }

    public double getDistance(Node node) {
      return getDistance(node.x, node.y);
    }

    public double getDistance(double x2, double y2) {
      double dx = x - x2;
      double dy = y - y2;
      return Math.sqrt(dx * dx + dy * dy);
    }

    public Point2D.Double getPoint() {
      return new Point2D.Double(x, y);
    }
  }

  public static class RailwayPath {
    public List<Node> nodes;
    public double distance;
  }

  private static class QueueNode implements Comparable<QueueNode> {
    private final Node node;
    private final Node parent;
    private final double score;
    private final double heuristicScore;

    public QueueNode(Node node, Node parent, double score, double heuristicScore) {
      this.node = node;
      this.parent = parent;
      this.score = score;
      this.heuristicScore = heuristicScore;
    }

    @Override
    public int compareTo(QueueNode other) {
      return Double.compare(this.heuristicScore, other.heuristicScore);
    }
  }

}
