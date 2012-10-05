package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.awt.geom.Point2D;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.onebusaway.uk.network_rail.gtfs_realtime.graph.RailwayGraph.RailwayPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crosby.binary.BinaryParser;
import crosby.binary.Osmformat.DenseNodes;
import crosby.binary.Osmformat.HeaderBlock;
import crosby.binary.Osmformat.Node;
import crosby.binary.Osmformat.Relation;
import crosby.binary.Osmformat.Way;
import crosby.binary.file.BlockInputStream;

@Singleton
public class RailwayShapeService {

  private static final Logger _log = LoggerFactory.getLogger(RailwayShapeService.class);

  private RailwayGraph _graph = new RailwayGraph();

  public void readOsmShapeData(File path) throws IOException {
    OsmPbfParserImpl parser = new OsmPbfParserImpl(_graph);
    BlockInputStream in = new BlockInputStream(new BufferedInputStream(
        new FileInputStream(path)), parser);
    in.process();
    in.close();
    _graph.addEdge(592400065L, 30744028L);
    _graph.pruneIslandNodes();
    _graph.pruneClusters(16000);
  }
  
  public RailwayGraph getGraph() {
    return _graph;
  }

  public RailwayPath getPath(Point2D.Double pFrom, Point2D.Double pTo) {
    RailwayGraph.Node nodeFrom = _graph.getClosestNode(pFrom.x, pFrom.y);
    RailwayGraph.Node nodeTo = _graph.getClosestNode(pTo.x, pTo.y);
    return _graph.getPath(nodeFrom, nodeTo);
  }

  private static class OsmPbfParserImpl extends BinaryParser {

    private RailwayGraph _graph;

    private static Set<String> _acceptableRailwayTypes = new HashSet<String>();

    static {
      _acceptableRailwayTypes.add("rail");
    }

    private Set<String> _railwayTypes = new HashSet<String>();

    private int _nodeCount = 0;

    public OsmPbfParserImpl(RailwayGraph graph) {
      _graph = graph;
    }

    @Override
    protected void parse(HeaderBlock header) {

    }

    @Override
    protected void parseDense(DenseNodes nodes) {
      long lastId = 0;
      long lastLat = 0;
      long lastLon = 0;
      for (int i = 0; i < nodes.getIdCount(); ++i) {
        long id = nodes.getId(i) + lastId;
        long lat = nodes.getLat(i) + lastLat;
        long lon = nodes.getLon(i) + lastLon;
        _graph.addNode(id, parseLat(lat), parseLon(lon));
        lastId = id;
        lastLat = lat;
        lastLon = lon;
        _nodeCount++;
        if (_nodeCount % 1000 == 0) {
          _log.info("nodes=" + _nodeCount);
        }
      }
    }

    @Override
    protected void parseNodes(List<Node> nodes) {
      for (Node node : nodes) {
        long id = node.getId();
        double lat = parseLat(node.getLat());
        double lon = parseLon(node.getLon());
        _graph.addNode(id, lat, lon);
        _nodeCount++;
        if (_nodeCount % 1000 == 0) {
          _log.info("nodes=" + _nodeCount);
        }
      }
    }

    @Override
    protected void parseWays(List<Way> ways) {
      for (Way way : ways) {
        Map<String, String> properties = parseProperties(way);
        if (isRail(properties)) {
          long lastId = 0;
          for (int i = 0; i < way.getRefsCount(); ++i) {
            long id = way.getRefs(i) + lastId;
            if (i > 0) {
              _graph.addEdge(lastId, id);
            }
            lastId = id;
          }
        }
      }
    }

    @Override
    protected void parseRelations(List<Relation> relations) {

    }

    @Override
    public void complete() {

    }

    private boolean isRail(Map<String, String> properties) {
      String rail = properties.get("railway");
      if (rail == null) {
        return false;
      }
      if (_railwayTypes.add(rail)) {
        System.out.println("railway=" + rail);
      }
      return _acceptableRailwayTypes.contains(rail);
    }

    private Map<String, String> parseProperties(Way way) {
      Map<String, String> properties = new HashMap<String, String>();
      for (int i = 0; i < way.getKeysCount(); ++i) {
        properties.put(getStringById(way.getKeys(i)),
            getStringById(way.getVals(i)));
      }
      return properties;
    }

  }
}
