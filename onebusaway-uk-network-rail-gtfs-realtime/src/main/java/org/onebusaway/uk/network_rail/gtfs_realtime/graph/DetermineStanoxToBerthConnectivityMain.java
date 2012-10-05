package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.uk.network_rail.gtfs_realtime.NetworkRailGtfsRealtimeModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrackIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class DetermineStanoxToBerthConnectivityMain {

  private static final Logger _log = LoggerFactory.getLogger(DetermineStanoxToBerthConnectivityMain.class);

  private static final String ARG_INPUT_GRAPH_PATH = "inputGraphPath";

  private static final String ARG_OUTPUT_GRAPH_PATH = "outputGraphPath";

  public static void main(String[] args) throws ParseException, IOException {
    DetermineStanoxToBerthConnectivityMain m = new DetermineStanoxToBerthConnectivityMain();
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

    RawStanoxNode node = (RawStanoxNode) _graph.getNode(TrackIdentifier.getStanoxIdentifier(87972));
    List<Ordered<String>> what = new ArrayList<Ordered<String>>();
    for (Map.Entry<RawNode, List<Integer>> entry : node.getIncoming().entrySet()) {
      RawNode to = entry.getKey();
      int avg = RawNode.average(entry.getValue());
      what.add(new Ordered<String>(to + " " + avg, entry.getValue().size()));
    }
    Collections.sort(what);
    for (Ordered<String> key : what) {
      System.out.println(key.toString());
    }
    _graph.write(new File(cli.getOptionValue(ARG_OUTPUT_GRAPH_PATH)));
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_INPUT_GRAPH_PATH, true, "input graph path");
    options.addOption(ARG_OUTPUT_GRAPH_PATH, true, "output graph path");
  }
}
