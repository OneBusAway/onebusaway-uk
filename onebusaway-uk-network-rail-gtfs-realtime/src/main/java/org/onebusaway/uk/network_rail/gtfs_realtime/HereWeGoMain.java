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
package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.atoc.timetable_parser.TimetableBundle;
import org.onebusaway.uk.network_rail.cif.TiplocInsertElement;
import org.onebusaway.uk.parser.DefaultContentHandler;
import org.onebusaway.uk.parser.Element;

public class HereWeGoMain {

  public static void main(String[] args) throws IOException {
    HereWeGoMain m = new HereWeGoMain();
    m.run();
  }

  private Map<String, StationElement> _stationsByTiploc = new HashMap<String, StationElement>();

  private Map<String, List<String>> _stanoxByTiploc = new HashMap<String, List<String>>();

  private Map<String, List<String>> _tiplocByStanox = new HashMap<String, List<String>>();

  private Map<String, Set<String>> _berthsByStanox = new HashMap<String, Set<String>>();

  private Set<String> _stanox = new HashSet<String>();

  public void run() throws IOException {
    TimetableBundle bundle = new TimetableBundle(new File(
        "/Users/bdferris/Downloads/uk-rail/data.atoc.org"));
    bundle.readMasterStationNames(new MasterStationNameHandler());
    bundle.readTimetable(new ScheduleHandler());

    System.out.println("stanoxCount=" + _stanox.size());

    File path = new File("/Users/bdferris/Downloads/uk-rail/berths.csv");
    BufferedReader reader = new BufferedReader(new FileReader(path));
    String line = null;

    Set<String> areasAndStanoxes = new HashSet<String>();
    int emptyStanox = 0;
    int duplicate = 0;
    int noTiplocs = 0;
    int noStation = 0;
    int total = 0;

    while ((line = reader.readLine()) != null) {
      String[] tokens = line.split(",");
      String area = tokens[0];
      String from = area + "_" + tokens[2];
      String to = area + "_" + tokens[3];
      String stanox = tokens[4];

      if (stanox.isEmpty()) {
        emptyStanox++;
        continue;
      }

      if (!areasAndStanoxes.add(area + "_" + stanox)) {
        duplicate++;
        continue;
      }
      List<String> tiplocs = _tiplocByStanox.get(stanox);
      if (tiplocs == null) {
        noTiplocs++;
        continue;
      }
      for (String tiploc : tiplocs) {
        StationElement station = _stationsByTiploc.get(tiploc);
        if (station == null) {
          noStation++;
          continue;
        }
        System.out.println(area + "," + stanox + "," + tiploc + ","
            + station.getLat() + "," + station.getLon());
        total++;
      }
    }

    System.out.println("emptyStanox=" + emptyStanox);
    System.out.println("duplicate=" + duplicate);
    System.out.println("noTiplocs=" + noTiplocs);
    System.out.println("noStation=" + noStation);
    System.out.println("total=" + total);
  }

  private void addBerthForStanox(String berthId, String stanox) {
    Set<String> berths = _berthsByStanox.get(stanox);
    if (berths == null) {
      berths = new HashSet<String>();
      _berthsByStanox.put(stanox, berths);
    }
    berths.add(berthId);
  }

  private void exploreIn(Node node, int depth, Graph graph) {
    if (depth <= 0) {
      return;
    }
    for (Node incoming : node.getIncoming()) {
      graph.addEdge(incoming.getId(), node.getId());
      exploreIn(incoming, depth - 1, graph);
    }
  }

  private void exploreOut(Node node, int depth, Graph graph) {
    if (depth <= 0) {
      return;
    }
    for (Node outgoing : node.getOutgoing()) {
      graph.addEdge(node.getId(), outgoing.getId());
      exploreOut(outgoing, depth - 1, graph);
    }
  }

  private class MasterStationNameHandler extends DefaultContentHandler {

    @Override
    public void startElement(Element element) {
      if (element instanceof StationElement) {
        StationElement station = (StationElement) element;
        _stationsByTiploc.put(station.getTiploc(), station);
      }
    }
  }

  private class ScheduleHandler extends DefaultContentHandler {

    @Override
    public void endElement(Element element) {
      if (element instanceof TiplocInsertElement) {
        TiplocInsertElement insert = (TiplocInsertElement) element;
        String stanox = Integer.toString(insert.getStanox());
        String tiploc = insert.getTiploc();
        List<String> stanoxes = _stanoxByTiploc.get(tiploc);
        if (stanoxes == null) {
          stanoxes = new ArrayList<String>();
          _stanoxByTiploc.put(tiploc, stanoxes);
        }

        stanoxes.add(stanox);
        List<String> tiplocs = _tiplocByStanox.get(stanox);
        if (tiplocs == null) {
          tiplocs = new ArrayList<String>();
          _tiplocByStanox.put(stanox, tiplocs);
        }
        tiplocs.add(tiploc);
      }
    }
  }

  private class Graph {
    private Map<String, Node> nodesById = new HashMap<String, HereWeGoMain.Node>();

    public void addEdge(String fromId, String toId) {
      Node from = getNode(fromId);
      Node to = getNode(toId);
      from.addOutgoing(to);
      to.addIncoming(from);
    }

    public Node getNode(String id) {
      Node node = nodesById.get(id);
      if (node == null) {
        node = new Node(id);
        nodesById.put(id, node);
      }
      return node;
    }

    public Iterable<Node> getNodes() {
      return nodesById.values();
    }
  }

  private class Node {

    private final String id;

    private Set<Node> outgoing = new HashSet<Node>();

    private Set<Node> incoming = new HashSet<Node>();

    public Node(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    public String getDotId() {
      return id.replaceAll("\\*", "_");
    }

    public void addIncoming(Node from) {
      incoming.add(from);
    }

    public void addOutgoing(Node to) {
      outgoing.add(to);
    }

    public Iterable<Node> getIncoming() {
      return incoming;
    }

    public Iterable<Node> getOutgoing() {
      return outgoing;
    }
  }
}
