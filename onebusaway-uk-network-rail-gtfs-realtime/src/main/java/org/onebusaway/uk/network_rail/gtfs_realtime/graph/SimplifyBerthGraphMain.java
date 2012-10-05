package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.uk.network_rail.gtfs_realtime.NetworkRailGtfsRealtimeModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthStepIdentifier;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrackIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class SimplifyBerthGraphMain {

  private static final Logger _log = LoggerFactory.getLogger(SimplifyBerthGraphMain.class);

  private static final String ARG_INPUT_GRAPH_PATH = "inputGraphPath";

  private static final String ARG_OUTPUT_GRAPH_PATH = "outputGraphPath";

  public static void main(String[] args) throws ParseException, IOException {
    SimplifyBerthGraphMain m = new SimplifyBerthGraphMain();
    m.run(args);
  }

  private RawGraph _graph = new RawGraph();

  private void run(String[] args) throws ParseException, IOException {
    Options options = new Options();
    buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    Set<Module> modules = new HashSet<Module>();
    NetworkRailGtfsRealtimeModule.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    _graph.read(new File(cli.getOptionValue(ARG_INPUT_GRAPH_PATH)));

    List<RawBerthNode> berthNodes = new ArrayList<RawBerthNode>();
    List<RawStanoxNode> stanoxNodes = new ArrayList<RawStanoxNode>();
    for (RawNode node : _graph.getNodes()) {
      if (node instanceof RawBerthNode) {
        berthNodes.add((RawBerthNode) node);
      } else if (node instanceof RawStanoxNode) {
        stanoxNodes.add((RawStanoxNode) node);
      }
    }

    RawStanoxNode a = (RawStanoxNode) _graph.getNode(TrackIdentifier.getStanoxIdentifier(3054));
    RawBerthNode b = (RawBerthNode) _graph.getNode(TrackIdentifier.getBerthIdentifier("WH_0347-WH_COUT"));
    System.out.println(a.getIncoming().get(b));
    System.out.println(a.getOutgoing().get(b));

    _log.info("stanox edges=" + countStanoxEdges(stanoxNodes));
    System.out.println(a.getIncoming().keySet());
    System.out.println(a.getOutgoing().keySet());

    for (RawStanoxNode node : stanoxNodes) {
      pruneStanox(node.getIncoming().keySet(), 0);
      pruneStanox(node.getOutgoing().keySet(), 0);
      pruneStanox(node.getIncoming().keySet(), node.getId().getStanox());
      pruneStanox(node.getOutgoing().keySet(), node.getId().getStanox());
    }

    _log.info("stanox edges=" + countStanoxEdges(stanoxNodes));
     System.out.println(a.getIncoming().keySet());
     System.out.println(a.getOutgoing().keySet());
    pruneLongStanoxToBerthEdges(stanoxNodes, 15 * 60);

    _log.info("stanox edges=" + countStanoxEdges(stanoxNodes));
    // System.out.println(a.getIncoming().keySet());
    // System.out.println(a.getOutgoing().keySet());

    for (RawStanoxNode node : stanoxNodes) {
      pruneInfrequentStanoxToBerthEdges(node, 0.25);
    }

    _log.info("stanox edges=" + countStanoxEdges(stanoxNodes));
    // System.out.println(a.getIncoming().keySet());
    // System.out.println(a.getOutgoing().keySet());

    for (RawStanoxNode node : stanoxNodes) {
      pruneRedundantBerthStepEdges(node, true);
      pruneRedundantBerthStepEdges(node, false);
    }

    _log.info("stanox edges=" + countStanoxEdges(stanoxNodes));
    // System.out.println(a.getIncoming().keySet());
    // System.out.println(a.getOutgoing().keySet());

    for (RawStanoxNode node : stanoxNodes) {
      pruneDistantBerthEdges(node, 15);
    }

    _log.info("stanox edges=" + countStanoxEdges(stanoxNodes));
    // System.out.println(a.getIncoming().keySet());
    // System.out.println(a.getOutgoing().keySet());

    _log.info("edges=" + countEdges(berthNodes));
    pruneSelfEdges(berthNodes);

    _log.info("edges=" + countEdges(berthNodes));
    Map<RawBerthNode, Set<RawBerthNode>> directParents = new HashMap<RawBerthNode, Set<RawBerthNode>>();
    pruneIndirectBerthEdges(berthNodes, directParents);

    _log.info("edges=" + countEdges(berthNodes));
    pruneIndirectParentBerthEdges(berthNodes, directParents);

    _log.info("edges=" + countEdges(berthNodes));
    pruneInfrequentBerthEdges(berthNodes, 0.25);

    _log.info("edges=" + countEdges(berthNodes));
    pruneLongBerthEdges(berthNodes, 15 * 60);

    _log.info("edges=" + countEdges(berthNodes));

    _graph.write(new File(cli.getOptionValue(ARG_OUTPUT_GRAPH_PATH)));
  }

  private int countEdges(List<RawBerthNode> nodes) {
    int edgeCount = 0;
    for (RawBerthNode node : nodes) {
      edgeCount += node.getOutgoing().size();
    }
    return edgeCount;
  }

  private int countStanoxEdges(List<RawStanoxNode> nodes) {
    int edgeCount = 0;
    for (RawStanoxNode node : nodes) {
      edgeCount += node.getOutgoing().size() + node.getIncoming().size();
    }
    return edgeCount;
  }

  /**
   * Prune any references from this node to itself.
   * 
   * @param node
   */
  private void pruneSelfEdges(List<RawBerthNode> nodes) {
    for (RawBerthNode node : nodes) {
      node.removeOutgoing(node);
    }
  }

  /**
   * If a node has a direct connection to one or more of its outgoing nodes, as
   * determined by
   * {@link BerthStepIdentifier#isDirectlyLinked(BerthStepIdentifier)}, we prune
   * all but the direct-linked node.
   * 
   * @param berthNodes
   * @param directParents
   */
  private void pruneIndirectBerthEdges(List<RawBerthNode> berthNodes,
      Map<RawBerthNode, Set<RawBerthNode>> directParents) {
    for (RawBerthNode node : berthNodes) {
      Set<RawBerthNode> direct = new HashSet<RawBerthNode>();
      for (RawBerthNode edge : node.getOutgoing().keySet()) {
        if (node.isDirectlyLinked(edge)) {
          direct.add(edge);
          Set<RawBerthNode> parents = directParents.get(edge);
          if (parents == null) {
            parents = new HashSet<RawBerthNode>();
            directParents.put(edge, parents);
          }
          parents.add(node);
        }
      }
      if (!direct.isEmpty()) {
        node.getOutgoing().keySet().retainAll(direct);
      }
    }
  }

  /**
   * In {@link #pruneIndirectBerthEdges(List, Map)}, we prune indirect outgoing
   * edges from a node if the node had directly linked edges. In this follow-up
   * step, we prune indirect incoming edges to a node if that node has
   * directly-linked incoming edges.
   * 
   * @param berthNodes
   * @param directParents
   */
  private void pruneIndirectParentBerthEdges(List<RawBerthNode> berthNodes,
      Map<RawBerthNode, Set<RawBerthNode>> directParents) {
    for (RawBerthNode node : berthNodes) {
      for (Iterator<RawBerthNode> it = node.getOutgoing().keySet().iterator(); it.hasNext();) {
        RawBerthNode edge = it.next();
        Set<RawBerthNode> parents = directParents.get(edge);
        if (parents != null && !parents.contains(node)) {
          it.remove();
        }
      }
    }
  }

  /**
   * 
   * @param nodes
   * @param minRatio
   */
  private void pruneInfrequentBerthEdges(List<RawBerthNode> nodes,
      double minRatio) {
    for (RawBerthNode node : nodes) {
      int maxCount = 0;
      for (List<Integer> durations : node.getOutgoing().values()) {
        maxCount = Math.max(maxCount, durations.size());
      }
      Set<RawBerthNode> nodesToRemove = new HashSet<RawBerthNode>();
      for (Map.Entry<RawBerthNode, List<Integer>> entry : node.getOutgoing().entrySet()) {
        RawBerthNode edge = entry.getKey();
        /**
         * We never prune a direct edge, even if it is infrequent.
         */
        if (node.isDirectlyLinked(edge)) {
          continue;
        }
        List<Integer> durations = entry.getValue();
        if (durations.size() < maxCount * minRatio) {
          nodesToRemove.add(edge);
        }
      }
      for (RawBerthNode toRemove : nodesToRemove) {
        List<Integer> durations = node.getOutgoingDurations(toRemove);
        node.removeOutgoing(toRemove);
        /**
         * We don't want to remove an edge from the graph if it breaks a path.
         */
        RawGraph.BerthPath path = _graph.getShortestPath(node, toRemove);
        if (path == null) {
          node.setOutgoing(toRemove, durations);
        }
      }
    }
  }

  private void pruneLongBerthEdges(List<RawBerthNode> nodes,
      int maxDurationInSeconds) {
    for (RawBerthNode node : nodes) {
      int maxCount = 0;
      for (List<Integer> durations : node.getOutgoing().values()) {
        maxCount = Math.max(maxCount, durations.size());
      }
      Set<RawBerthNode> nodesToRemove = new HashSet<RawBerthNode>();
      for (Map.Entry<RawBerthNode, List<Integer>> entry : node.getOutgoing().entrySet()) {
        RawBerthNode edge = entry.getKey();
        /**
         * We never prune a direct edge, even if it is overly long.
         */
        if (node.isDirectlyLinked(edge)) {
          continue;
        }
        List<Integer> durations = entry.getValue();
        int averageDuration = RawNode.average(durations);
        if (averageDuration > maxDurationInSeconds) {
          nodesToRemove.add(edge);
        }
      }
      node.getOutgoing().keySet().removeAll(nodesToRemove);
    }
  }

  private void pruneStanox(Set<RawNode> nodes, int stanox) {
    for (Iterator<RawNode> it = nodes.iterator(); it.hasNext();) {
      RawNode node = it.next();
      if (node instanceof RawStanoxNode) {
        RawStanoxNode rawStanoxNode = (RawStanoxNode) node;
        if (rawStanoxNode.getId().getStanox() == stanox) {
          it.remove();
        }
      }
    }
  }

  /**
   * Prune stanox-to-berth edges whose average duration is more than the
   * specified maximum.
   * 
   * @param stanoxNodes
   * @param maxDurationInSeconds
   */
  private void pruneLongStanoxToBerthEdges(List<RawStanoxNode> stanoxNodes,
      int maxDurationInSeconds) {
    for (RawStanoxNode node : stanoxNodes) {
      pruneLongStanoxToBerthEdges(node.getIncoming(), maxDurationInSeconds);
      pruneLongStanoxToBerthEdges(node.getOutgoing(), maxDurationInSeconds);
    }
  }

  private void pruneLongStanoxToBerthEdges(Map<RawNode, List<Integer>> edges,
      int maxDurationInSeconds) {
    for (Iterator<Map.Entry<RawNode, List<Integer>>> it = edges.entrySet().iterator(); it.hasNext();) {
      Map.Entry<RawNode, List<Integer>> entry = it.next();
      RawNode node = entry.getKey();
      if (node instanceof RawStanoxNode) {
        continue;
      }
      int avgDuration = RawNode.average(entry.getValue());
      if (avgDuration > maxDurationInSeconds) {
        it.remove();
      }
    }
  }

  private void pruneInfrequentStanoxToBerthEdges(RawStanoxNode node,
      double ratio) {
    int maxCount = 0;
    maxCount = getMaxFrequencyForEdges(node.getIncoming(), maxCount);
    maxCount = getMaxFrequencyForEdges(node.getOutgoing(), maxCount);
    int threshold = (int) (maxCount * ratio);
    pruneEdgesWithLowFrequency(node.getIncoming(), threshold);
    pruneEdgesWithLowFrequency(node.getOutgoing(), threshold);
  }

  private int getMaxFrequencyForEdges(Map<RawNode, List<Integer>> edges,
      int maxCount) {
    for (Map.Entry<RawNode, List<Integer>> entry : edges.entrySet()) {
      if (entry.getKey() instanceof RawBerthNode) {
        maxCount = Math.max(maxCount, entry.getValue().size());
      }
    }
    return maxCount;
  }

  private void pruneEdgesWithLowFrequency(Map<RawNode, List<Integer>> edges,
      int threshold) {
    for (Iterator<Map.Entry<RawNode, List<Integer>>> it = edges.entrySet().iterator(); it.hasNext();) {
      Map.Entry<RawNode, List<Integer>> entry = it.next();
      if (entry.getKey() instanceof RawBerthNode
          && entry.getValue().size() < threshold) {
        it.remove();
      }
    }
  }

  private void pruneRedundantBerthStepEdges(RawStanoxNode node, boolean forward) {
    Map<String, RawBerthNode> fromBerths = new HashMap<String, RawBerthNode>();
    Map<String, RawBerthNode> toBerths = new HashMap<String, RawBerthNode>();
    Map<RawNode, List<Integer>> edges = forward ? node.getOutgoing()
        : node.getIncoming();
    for (RawNode edge : edges.keySet()) {
      if (edge instanceof RawBerthNode) {
        RawBerthNode berthNode = (RawBerthNode) edge;
        BerthStepIdentifier id = berthNode.getId();
        fromBerths.put(id.getFromBerthId(), berthNode);
        toBerths.put(id.getToBerthId(), berthNode);
      }
    }
    if (forward) {
      fromBerths.keySet().retainAll(toBerths.keySet());
      node.getOutgoing().keySet().removeAll(fromBerths.values());
    } else {
      toBerths.keySet().retainAll(fromBerths.keySet());
      node.getIncoming().keySet().removeAll(toBerths.values());
    }
  }

  private void pruneDistantBerthEdges(RawStanoxNode node, double factor) {
    int minTime = Integer.MAX_VALUE;
    minTime = getMinTimeForBerthEdges(minTime, node.getIncoming());
    minTime = getMinTimeForBerthEdges(minTime, node.getOutgoing());
    int timeThreshold = (int) (minTime * factor);
    pruneDistanceBerthEdges(node.getIncoming(), timeThreshold);
    pruneDistanceBerthEdges(node.getOutgoing(), timeThreshold);
  }

  private int getMinTimeForBerthEdges(int minTime,
      Map<RawNode, List<Integer>> edges) {
    for (Map.Entry<RawNode, List<Integer>> entry : edges.entrySet()) {
      if (entry.getKey() instanceof RawBerthNode) {
        int avg = RawNode.average(entry.getValue());
        minTime = Math.min(minTime, avg);
      }
    }
    return minTime;
  }

  private void pruneDistanceBerthEdges(Map<RawNode, List<Integer>> edges,
      int timeThreshold) {
    for (Iterator<Map.Entry<RawNode, List<Integer>>> it = edges.entrySet().iterator(); it.hasNext();) {
      Map.Entry<RawNode, List<Integer>> entry = it.next();
      if (entry.getKey() instanceof RawBerthNode) {
        int avg = RawNode.average(entry.getValue());
        if (avg > timeThreshold) {
          it.remove();
        }
      }
    }
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_INPUT_GRAPH_PATH, true, "input graph path");
    options.addOption(ARG_OUTPUT_GRAPH_PATH, true, "output graph path");
  }
}
