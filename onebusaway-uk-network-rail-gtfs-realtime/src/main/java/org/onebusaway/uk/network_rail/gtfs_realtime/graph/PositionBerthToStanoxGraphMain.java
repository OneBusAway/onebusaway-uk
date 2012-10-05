package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.collections.Min;
import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.NetworkRailGtfsRealtimeModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.TimetableService;
import org.onebusaway.uk.network_rail.gtfs_realtime.graph.RailwayGraph.Node;
import org.onebusaway.uk.network_rail.gtfs_realtime.graph.RailwayGraph.RailwayPath;
import org.onebusaway.uk.network_rail.gtfs_realtime.graph.RawGraph.BerthPath;
import org.onebusaway.uk.parser.ProjectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.actors.threadpool.Arrays;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class PositionBerthToStanoxGraphMain {

  private static Logger _log = LoggerFactory.getLogger(PositionBerthToStanoxGraphMain.class);

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_GRAPH_PATH = "graphPath";

  private static final String ARG_STATIONS_PATH = "stationsPath";

  private static final String ARG_OSM_SHAPES_PATH = "osmShapesPath";

  public static void main(String[] args) throws ParseException, IOException {
    PositionBerthToStanoxGraphMain m = new PositionBerthToStanoxGraphMain();
    m.run(args);
  }

  private RawGraph _graph = new RawGraph();

  private TimetableService _timetableService;

  private RailwayShapeService _railwayShapeService;

  private GraphLayout _graphLayout;

  private Map<RawStanoxNode, Location> _stanoxLocations = new HashMap<RawStanoxNode, Location>();

  private Map<RawBerthNode, Location> _berthNodesToLocations = new HashMap<RawBerthNode, Location>();

  private Map<RawBerthNode, List<Point2D.Double>> _berthNodesToPotentialLocations = new HashMap<RawBerthNode, List<Point2D.Double>>();

  @Inject
  public void setTimetableService(TimetableService timetableService) {
    _timetableService = timetableService;
  }

  @Inject
  public void setRailwayShapeService(RailwayShapeService railwayShapeService) {
    _railwayShapeService = railwayShapeService;
  }

  @Inject
  public void setGraphLayout(GraphLayout graphLayout) {
    _graphLayout = graphLayout;
  }

  private void run(String[] args) throws ParseException, IOException {
    Options options = new Options();
    buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    Set<Module> modules = new HashSet<Module>();
    NetworkRailGtfsRealtimeModule.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    _timetableService.readScheduleData(new File(
        cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));
    if (cli.hasOption(ARG_STATIONS_PATH)) {
      _timetableService.readStationLocations(new File(
          cli.getOptionValue(ARG_STATIONS_PATH)));
    }
    if (cli.hasOption(ARG_OSM_SHAPES_PATH)) {
      _railwayShapeService.readOsmShapeData(new File(
          cli.getOptionValue(ARG_OSM_SHAPES_PATH)));
    }

    _graph.read(new File(cli.getOptionValue(ARG_GRAPH_PATH)));

    buildStanoxLocations();
    setBerthLocationsFromStanox();

    loop();
    exportLocationsToKml();
  }

  private Location averagePoints(List<Point2D.Double> points) {
    double xTotal = 0;
    double yTotal = 0;
    for (Point2D.Double point : points) {
      xTotal += point.x;
      yTotal += point.y;
    }
    Location l = new Location();
    l.x = xTotal / points.size();
    l.y = yTotal / points.size();
    Point2D.Double p = ProjectionSupport.convertToLatLon(l.x, l.y);
    l.lat = p.y;
    l.lon = p.x;
    return l;

  }

  private void buildOptions(Options options) {
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_GRAPH_PATH, true, "graph path");
    options.addOption(ARG_STATIONS_PATH, true, "");
    options.addOption(ARG_OSM_SHAPES_PATH, true, "");
  }

  private void buildStanoxLocations() {
    long stanoxCount = 0;
    long stanoxWithLocation = 0;
    for (RawNode node : _graph.getNodes()) {
      if (node instanceof RawStanoxNode) {
        stanoxCount++;
        RawStanoxNode stanoxNode = (RawStanoxNode) node;
        Point2D.Double point = _timetableService.getBestLocationForStanox(stanoxNode.getId().getStanox());
        if (point != null) {
          Location location = new Location(point);

          _stanoxLocations.put(stanoxNode, location);
          stanoxWithLocation++;
        }
      }
    }
    _log.info("stanox with location=" + stanoxWithLocation + "/" + stanoxCount);
  }

  private void setBerthLocationsFromStanox() {
    long total = 0;
    long hits = 0;
    for (RawBerthNode node : _graph.getBerthNodes()) {
      if (node.getId().toString().equals("WH_0347-WH_COUT")) {
        System.out.println("here");
      }
      Location location = getStanoxLocationForBerth(node);
      if (location != null) {
        hits++;
        _berthNodesToLocations.put(node, location);
      }
      total++;
    }
    _log.info("berth nodes attached directly to stanox=" + hits + "/" + total);
  }

  private Location getStanoxLocationForBerth(RawBerthNode node) {
    for (RawStanoxNode stanoxNode : node.getStanox()) {
      Location location = _stanoxLocations.get(stanoxNode);
      if (location != null) {
        return location;
      }
    }
    return null;
  }

  private void loop() {
    while (true) {
      _log.info("nodes with locations=" + _berthNodesToLocations.size());
      interpolateBerthLocations();
      if (_berthNodesToPotentialLocations.isEmpty()) {
        return;
      }
      averageBerthLocations();
    }
  }

  private void interpolateBerthLocations() {
    int index = 0;
    for (RawBerthNode rootNode : _berthNodesToLocations.keySet()) {
      if (index % 100 == 0) {
        _log.info("node=" + index + "/"
            + _berthNodesToLocations.keySet().size());
      }
      index++;
      Location fromLocation = _berthNodesToLocations.get(rootNode);
      Queue<OrderedRawBerthNode> queue = new PriorityQueue<OrderedRawBerthNode>();
      queue.add(new OrderedRawBerthNode(rootNode, null, 0.0));

      Map<RawBerthNode, RawBerthNode> parents = new HashMap<RawBerthNode, RawBerthNode>();
      Set<RawBerthNode> visited = new HashSet<RawBerthNode>();

      while (!queue.isEmpty()) {
        OrderedRawBerthNode currentNode = queue.poll();
        RawBerthNode node = currentNode.getNode();
        if (!visited.add(node)) {
          continue;
        }

        parents.put(node, currentNode.getParent());
        Location toLocation = _berthNodesToLocations.get(node);
        if (currentNode.getParent() != null && toLocation != null) {

          List<RawBerthNode> path = new ArrayList<RawBerthNode>();
          RawBerthNode last = node;
          while (last != null) {
            path.add(last);
            last = parents.get(last);
          }

          if (path.size() <= 2) {
            break;
          }
          Collections.reverse(path);
          BerthPath berthPath = new BerthPath(path, currentNode.getDistance());
          double d = fromLocation.getDistance(toLocation);
          if (d > 30000) {
            continue;
          }
          RailwayPath railwayPath = _railwayShapeService.getPath(
              fromLocation.getPoint(), toLocation.getPoint());
          if (railwayPath != null) {
            snapBerthsToRailwayPath(berthPath, railwayPath);
          }
          break;
        } else {
          for (Map.Entry<RawBerthNode, List<Integer>> entry : node.getOutgoing().entrySet()) {
            RawBerthNode outgoing = entry.getKey();
            int avgDuration = RawNode.average(entry.getValue());
            queue.add(new OrderedRawBerthNode(outgoing, node,
                currentNode.getDistance() + avgDuration));
          }
        }
      }
    }
  }

  private void averageBerthLocations() {
    for (Map.Entry<RawBerthNode, List<Point2D.Double>> entry : _berthNodesToPotentialLocations.entrySet()) {
      Location p = averagePoints(entry.getValue());
      _berthNodesToLocations.put(entry.getKey(), p);
    }
    _berthNodesToPotentialLocations.clear();
  }

  private void explore2(RawStanoxNode stanoxNode) {
    Location p = _stanoxLocations.get(stanoxNode);
    if (p == null) {
      return;
    }

    for (RawNode edge : stanoxNode.getOutgoing().keySet()) {
      if (!(edge instanceof RawStanoxNode)) {
        continue;
      }
      RawStanoxNode nearbyStanoxNode = (RawStanoxNode) edge;
      Location pTo = _stanoxLocations.get(nearbyStanoxNode);
      if (pTo == null) {
        continue;
      }
      double distance = p.getDistance(pTo);
      if (distance > 50000) {
        continue;
      }
      _log.info("from=" + stanoxNode + " to=" + nearbyStanoxNode + " distance="
          + distance);
      _log.info(p.lat + " " + p.lon + " " + pTo.lat + " " + pTo.lon);

      BerthPath berthPath = getShortestPathBetweenNodes(stanoxNode,
          nearbyStanoxNode, distance);
      if (berthPath != null) {
        _log.info("railway path");
        RailwayPath railwayPath = _railwayShapeService.getPath(p.getPoint(),
            pTo.getPoint());
        if (railwayPath != null) {
          snapBerthsToRailwayPath(berthPath, railwayPath);
        }
      }
    }
  }

  private BerthPath getShortestPathBetweenNodes(RawStanoxNode fromStanoxNode,
      RawStanoxNode toStanoxNode, double distance) {
    double maxTime = (distance * 1.5 /* meters */) / (50 /* m/s */);
    Min<BerthPath> minPath = new Min<RawGraph.BerthPath>();
    for (RawBerthNode fromNode : fromStanoxNode.getBerthConnections()) {
      for (RawBerthNode toNode : toStanoxNode.getBerthConnections()) {
        BerthPath path = _graph.getShortestPath(fromNode, toNode, maxTime);
        if (path != null) {
          minPath.add(path.distance, path);
        }
      }
    }
    return minPath.getMinElement();
  }

  private void snapBerthsToRailwayPath(BerthPath berthPath,
      RailwayPath railwayPath) {
    double[] relativeBerthDistance = computeRelativeDistance(berthPath);
    double[] relativeShapeDistance = computeRelativeDistance(railwayPath);
    for (int i = 0; i < relativeBerthDistance.length; ++i) {
      double d = relativeBerthDistance[i];
      Point2D.Double p = getBestPoint(railwayPath.nodes, relativeShapeDistance,
          d);
      RawBerthNode node = berthPath.nodes.get(i);
      List<Point2D.Double> locations = _berthNodesToPotentialLocations.get(node);
      if (locations == null) {
        locations = new ArrayList<Point2D.Double>();
        _berthNodesToPotentialLocations.put(node, locations);
      }
      locations.add(p);
    }
  }

  private Point2D.Double getBestPoint(List<Node> nodes,
      double[] relativeShapeDistance, double d) {
    int index = Arrays.binarySearch(relativeShapeDistance, d);
    if (index >= 0) {
      return nodes.get(index).getPoint();
    }
    if (index < 0) {
      index = -(index + 1);
    }
    if (index == 0) {
      return nodes.get(0).getPoint();
    }
    if (index >= nodes.size()) {
      return nodes.get(nodes.size() - 1).getPoint();
    }
    Node nodeFrom = nodes.get(index - 1);
    Node nodeTo = nodes.get(index);
    double dFrom = relativeShapeDistance[index - 1];
    double dTo = relativeShapeDistance[index];
    double ratio = (d - dFrom) / (dTo - dFrom);
    double x = nodeFrom.getX() + ratio * (nodeTo.getX() - nodeFrom.getX());
    double y = nodeFrom.getY() + ratio * (nodeTo.getY() - nodeFrom.getY());
    return new Point2D.Double(x, y);
  }

  private double[] computeRelativeDistance(BerthPath berthPath) {
    double[] distances = new double[berthPath.nodes.size()];
    double totalDistance = berthPath.distance;
    double cumulativeDistance = 0.0;
    for (int i = 0; i < berthPath.nodes.size(); ++i) {
      RawBerthNode node = berthPath.nodes.get(i);
      if (i > 0) {
        RawBerthNode prev = berthPath.nodes.get(i - 1);
        double duration = prev.getOutgoingAverageDuration(node);
        cumulativeDistance += duration;
      }
      distances[i] = cumulativeDistance / totalDistance;
    }
    return distances;
  }

  private double[] computeRelativeDistance(RailwayPath railwayPath) {
    double[] distances = new double[railwayPath.nodes.size()];
    double totalDistance = railwayPath.distance;
    double cumulativeDistance = 0.0;
    for (int i = 0; i < railwayPath.nodes.size(); ++i) {
      RailwayGraph.Node node = railwayPath.nodes.get(i);
      if (i > 0) {
        RailwayGraph.Node prev = railwayPath.nodes.get(i - 1);
        double distance = prev.getDistance(node);
        cumulativeDistance += distance;
      }
      distances[i] = cumulativeDistance / totalDistance;
    }
    return distances;
  }

  private void explore(RawStanoxNode stanoxNode) {
    Set<RawBerthNode> connections = stanoxNode.getBerthConnections();
    if (connections.isEmpty()) {
      return;
    }
    explore(connections, stanoxNode);
  }

  private void explore(Set<RawBerthNode> connections, RawStanoxNode stanoxNode) {
    Queue<OrderedRawBerthNode> queue = new PriorityQueue<OrderedRawBerthNode>();
    Set<RawBerthNode> visited = new HashSet<RawBerthNode>();
    int openCount = 0;
    for (RawBerthNode connection : connections) {
      queue.add(new OrderedRawBerthNode(connection, null, 0.0));
      openCount++;
    }

    Map<RawBerthNode, RawBerthNode> parents = new HashMap<RawBerthNode, RawBerthNode>();

    while (!queue.isEmpty()) {
      OrderedRawBerthNode currentNode = queue.poll();
      RawBerthNode node = currentNode.getNode();
      boolean isOpen = currentNode.isOpen();
      if (isOpen) {
        openCount--;
      } else if (openCount == 0) {
        return;
      }
      if (visited.contains(node)) {
        continue;
      }
      visited.add(node);
      parents.put(node, currentNode.getParent());
      Set<RawStanoxNode> stanoxes = node.getStanox();
      if (stanoxes.size() > 0 && !stanoxes.contains(stanoxNode)) {
        _log.info(node + " stanoxes=" + stanoxes + " "
            + currentNode.getDistance() + " open=" + openCount);
        RawBerthNode c = node;
        while (c != null) {
          _log.info("  " + c);
          c = parents.get(c);
        }
        isOpen = false;
      }
      for (Map.Entry<RawBerthNode, List<Integer>> entry : node.getOutgoing().entrySet()) {
        RawBerthNode outgoing = entry.getKey();
        int avgDuration = RawNode.average(entry.getValue());
        queue.add(new OrderedRawBerthNode(outgoing, node,
            currentNode.getDistance() + avgDuration, isOpen));
        if (isOpen) {
          openCount++;
        }
      }
    }
  }

  private void fillLocationsForKnownStanox(
      Map<RawBerthNode, Location> nodesToLocations) {

  }

  private Location getLocationForStanox(int stanox) {
    double xTotal = 0;
    double yTotal = 0;
    int count = 0;

    for (String tiploc : _timetableService.getTiplocsForStanox(stanox)) {
      StationElement station = _timetableService.getStationForTiploc(tiploc);
      if (station != null) {
        xTotal += station.getEasting();
        yTotal += station.getNorthing();
        count++;
      }
    }

    if (count == 0) {
      return null;
    }

    Location location = new Location();
    location.fixed = true;
    location.x = xTotal / count;
    location.y = yTotal / count;
    return location;
  }

  private void fillLocationsForUnknownStanox(
      Map<RawBerthNode, Location> nodesToLocations) {

    int total = 0;
    int problem = 0;

    Map<RawBerthNode, Location> updates = new HashMap<RawBerthNode, Location>();

    _log.info("updates=" + updates.size());
    nodesToLocations.putAll(updates);
  }

  private Map<Location, Integer> getNearbyNodesWithLocation(
      Map<RawBerthNode, Location> nodesToLocations, RawBerthNode source,
      int minCount) {

    Map<Location, Integer> locationsAndTime = new HashMap<Location, Integer>();

    PriorityQueue<OrderedNode> queue = new PriorityQueue<OrderedNode>();
    queue.add(new OrderedNode(source, 0));

    Set<RawBerthNode> visited = new HashSet<RawBerthNode>();
    visited.add(source);

    Map<RawBerthNode, Integer> minTimeToSource = new HashMap<RawBerthNode, Integer>();

    while (!queue.isEmpty()) {
      OrderedNode orderedNode = queue.poll();
      RawBerthNode node = orderedNode.node;
      if (minTimeToSource.containsKey(node)) {
        continue;
      }
      int time = orderedNode.value;
      minTimeToSource.put(node, time);
      if (nodesToLocations.containsKey(node)) {
        locationsAndTime.put(nodesToLocations.get(node), time);
        if (locationsAndTime.size() >= minCount) {
          return locationsAndTime;
        }
      }

      for (Edge edge : node.getEdges()) {
        RawBerthNode to = edge.getTo();
        int proposedTime = edge.getAverageDuration() + time;
        if (!minTimeToSource.containsKey(to)) {
          queue.add(new OrderedNode(to, proposedTime));
        }
      }
    }

    return locationsAndTime;
  }

  private void exportLocationsToKml() throws FileNotFoundException {
    PrintWriter out = new PrintWriter(
        "/Users/bdferris/Documents/uk-rail/graph.kml");
    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
    out.println("<Document>");
    out.println("  <Style id=\"stanox\">");
    out.println("    <IconStyle>");
    out.println("      <color>ff000000</color>");
    out.println("    </IconStyle>");
    out.println("  </Style>");
    for (Map.Entry<RawStanoxNode, Location> entry : _stanoxLocations.entrySet()) {
      RawStanoxNode node = entry.getKey();
      Location location = entry.getValue();
      out.println("  <Placemark>");
      out.println("    <name>" + node.getId() + "</name>");
      out.println("    <styleUrl>stanox</styleUrl>");
      out.println("    <Point>");
      out.println("      <coordinates>" + location.lon + "," + location.lat
          + ",0</coordinates>");
      out.println("    </Point>");
      out.println("  </Placemark>");
    }
    for (Map.Entry<RawBerthNode, Location> entry : _berthNodesToLocations.entrySet()) {
      RawBerthNode node = entry.getKey();
      Location location = entry.getValue();
      out.println("  <Placemark>");
      out.println("    <name>" + node.getId() + "</name>");
      out.println("    <MultiGeometry>");
      out.println("      <Point>");
      out.println("        <coordinates>" + location.lon + "," + location.lat
          + ",0</coordinates>");
      out.println("      </Point>");
      for (RawBerthNode edge : node.getOutgoing().keySet()) {
        Location edgeLocation = _berthNodesToLocations.get(edge);
        if (edgeLocation != null) {
          out.println("      <LineString>");
          out.println("        <coordinates>" + location.lon + ","
              + location.lat + ",0 " + edgeLocation.lon + ","
              + edgeLocation.lat + ",0</coordinates>");
          out.println("      </LineString>");
        }
      }
      out.println("    </MultiGeometry>");
      out.println("  </Placemark>");
    }
    out.println("</Document>");
    out.println("</kml>");
    out.close();

    // RailwayGraph graph = _railwayShapeService.getGraph();
    // Set<RailwayGraph.Node> remaining = new HashSet<RailwayGraph.Node>(
    // graph.getNodes());
    // while (!remaining.isEmpty()) {
    // Node node = remaining.iterator().next();
    // while (true) {
    // if (!remaining.remove(node)) {
    // break;
    // }
    // }
    // List<Node> path = exploreRailwayPath(node, remaining);
    // }
  }

  private Location computeCentroid(Set<Location> locations) {
    double xTotal = 0;
    double yTotal = 0;
    for (Location location : locations) {
      xTotal += location.x;
      yTotal += location.y;
    }
    Location location = new Location();
    location.x = xTotal / locations.size();
    location.y = yTotal / locations.size();
    return location;
  }

  private static class OrderedNode implements Comparable<OrderedNode> {
    public final RawBerthNode node;
    public final int value;

    public OrderedNode(RawBerthNode node, int value) {
      this.node = node;
      this.value = value;
    }

    @Override
    public int compareTo(OrderedNode o) {
      return Double.compare(this.value, o.value);
    }
  }
}
