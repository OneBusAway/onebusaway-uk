package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.NetworkRailGtfsRealtimeModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.TimetableService;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class VisualizeStanoxLocationsMain {

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_OUTPUT_PATH = "outputPath";

  public static void main(String[] args) throws ParseException, IOException {
    VisualizeStanoxLocationsMain m = new VisualizeStanoxLocationsMain();
    m.run(args);
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

    TimetableService model = new TimetableService();
    model.readScheduleData(new File(cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));

    Map<String, String> regionToStyle = new HashMap<String, String>();

    PrintWriter writer = new PrintWriter(new File(
        cli.getOptionValue(ARG_OUTPUT_PATH)));
    writer.println("region,style,stanox,tiploc,lat,lon,name");

    for (int stanox : model.getAllStanox()) {
      String stanoxValue = Integer.toString(stanox);
      if (stanoxValue.length() < 2) {
        System.out.println(stanox);
        continue;
      }
      String region = stanoxValue.substring(0, 2);
      String style = regionToStyle.get(region);
      if (style == null) {
        style = IconStyles.SMALL[regionToStyle.size() % IconStyles.SMALL.length];
        regionToStyle.put(region, style);
      }
      for (String tiploc : model.getTiplocsForStanox(stanox)) {
        StationElement station = model.getStationForTiploc(tiploc);
        if (station != null) {
          writer.println(region + "," + style + "," + stanox + "," + tiploc
              + "," + station.getLat() + "," + station.getLon() + ","
              + station.getName());
        }
      }
    }

    writer.close();

  }

  private void buildOptions(Options options) {
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_OUTPUT_PATH, true, "output path");
  }

}
