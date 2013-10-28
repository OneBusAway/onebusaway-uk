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
package org.onebusaway.uk.atco_cif_to_gtfs_converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.uk.atco_cif.extensions.NationalExpressExtensionParser;
import org.onebusaway.uk.atco_cif.extensions.greater_manchester.GreaterManchesterParser;
import org.onebusaway.uk.parser.ParserException;

public class AtcoCifToGtfsConverterMain {

  private static final String ARG_AGENCY_ID = "agencyId";

  private static final String ARG_AGENCY_LANG = "agencyLang";

  private static final String ARG_AGENCY_NAME = "agencyName";

  private static final String ARG_AGENCY_PHONE = "agencyPhone";

  private static final String ARG_AGENCY_TIMEZONE = "agencyTimezone";

  private static final String ARG_AGENCY_URL = "agencyUrl";

  private static final String ARG_VEHICLE_TYPE = "vehicleType";

  private static final String ARG_PRUNE_STOPS_WITH_NO_LOCATION_INFO = "pruneStopsWithNoLocationInfo";

  private static final String ARG_PRUNE_STOPS_WITH_PREFIX = "pruneStopsWithPrefix";

  private static final String ARG_PRUNE_TRIPS_WITH_MISSING_STOPS = "pruneTripsWithMissingStops";

  private static final String ARG_LOCATION_SCALE_FACTOR = "locationScaleFactor";

  private static final String ARG_PREFERRED_DIRECTION_IDS_FOR_ROUTE_DETAILs = "preferredDirectionIdsForRouteDetails";

  private static final String ARG_NAPTAN_CSV_PATH = "naptanCsvPath";

  private static final String ARG_NATIONAL_EXPRESS_EXTENSIONS = "nationalExpressExtensions";

  private static final String ARG_GREATER_MANCHESTER_EXTENSIONS = "greaterManchesterExtensions";

  public static void main(String[] args) throws ParseException, IOException {
    AtcoCifToGtfsConverterMain m = new AtcoCifToGtfsConverterMain();
    m.run(args);
  }

  public void run(String[] args) throws ParseException, IOException {
    Options options = new Options();
    buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    if (args.length != 2) {
      usage();
      System.exit(-1);
    }

    AtcoCifToGtfsConverter converter = new AtcoCifToGtfsConverter();
    configureConverter(args, cli, converter);

    converter.run();
  }

  private void configureConverter(String[] args, CommandLine cli,
      AtcoCifToGtfsConverter converter) {

    converter.setInputPath(new File(args[0]));
    converter.setOutputPath(new File(args[1]));

    if (cli.hasOption(ARG_AGENCY_ID)) {
      converter.setAgencyId(cli.getOptionValue(ARG_AGENCY_ID));
    }
    if (cli.hasOption(ARG_AGENCY_LANG)) {
      converter.setAgencyLang(cli.getOptionValue(ARG_AGENCY_LANG));
    }
    if (cli.hasOption(ARG_AGENCY_NAME)) {
      converter.setAgencyName(cli.getOptionValue(ARG_AGENCY_NAME));
    }
    if (cli.hasOption(ARG_AGENCY_PHONE)) {
      converter.setAgencyPhone(cli.getOptionValue(ARG_AGENCY_PHONE));
    }
    if (cli.hasOption(ARG_AGENCY_TIMEZONE)) {
      converter.setAgencyTimezone(cli.getOptionValue(ARG_AGENCY_TIMEZONE));
    }
    if (cli.hasOption(ARG_AGENCY_URL)) {
      converter.setAgencyUrl(cli.getOptionValue(ARG_AGENCY_URL));
    }
    if (cli.hasOption(ARG_VEHICLE_TYPE)) {
      String value = cli.getOptionValue(ARG_VEHICLE_TYPE).toLowerCase();
      if (value.contains("tram") || value.contains("streetcar")
          || value.contains("lightrail")) {
        converter.setVehicleType(0);
      } else if (value.contains("subway") || value.contains("metro")) {
        converter.setVehicleType(1);
      } else if (value.contains("rail")) {
        converter.setVehicleType(2);
      } else if (value.contains("bus")) {
        converter.setVehicleType(3);
      } else if (value.contains("ferry")) {
        converter.setVehicleType(4);
      } else if (value.contains("cablecar")) {
        converter.setVehicleType(5);
      } else if (value.contains("gondola")) {
        converter.setVehicleType(6);
      } else if (value.contains("funicular")) {
        converter.setVehicleType(7);
      } else {
        try {
          converter.setVehicleType(Integer.parseInt(value));
        } catch (NumberFormatException ex) {
          throw new ParserException("unknown vehicle type argument specified: "
              + value);
        }
      }
    }
    converter.setPruneStopsWithNoLocationInfo(cli.hasOption(ARG_PRUNE_STOPS_WITH_NO_LOCATION_INFO));
    converter.setPruneTripsWithMissingStops(cli.hasOption(ARG_PRUNE_TRIPS_WITH_MISSING_STOPS));
    if (cli.hasOption(ARG_LOCATION_SCALE_FACTOR)) {
      double locationScaleFactor = Double.parseDouble(cli.getOptionValue(ARG_LOCATION_SCALE_FACTOR));
      converter.getParser().setLocationScaleFactor(locationScaleFactor);
    }

    if (cli.hasOption(ARG_PRUNE_STOPS_WITH_PREFIX)) {
      Set<String> pruneStopsWithPrefixes = new HashSet<String>();
      for (String prefix : cli.getOptionValues(ARG_PRUNE_STOPS_WITH_PREFIX)) {
        pruneStopsWithPrefixes.add(prefix);
      }
      if (!pruneStopsWithPrefixes.isEmpty()) {
        converter.setPruneStopsWithPrefixes(pruneStopsWithPrefixes);
      }
    }
    if (cli.hasOption(ARG_PREFERRED_DIRECTION_IDS_FOR_ROUTE_DETAILs)) {
      String ids = cli.getOptionValue(ARG_PREFERRED_DIRECTION_IDS_FOR_ROUTE_DETAILs);
      converter.setPreferredDirectionIdsForRouteDetails(Arrays.asList(ids.split(",")));
    }
    if (cli.hasOption(ARG_NAPTAN_CSV_PATH)) {
      converter.setNaptanCsvPath(new File(
          cli.getOptionValue(ARG_NAPTAN_CSV_PATH)));
    }
    if (cli.hasOption(ARG_NATIONAL_EXPRESS_EXTENSIONS)) {
      NationalExpressExtensionParser parser = new NationalExpressExtensionParser();
      for (String extenstionType : parser.getSupportedTypes()) {
        converter.getParser().addExtension(extenstionType, parser);
      }
    }
    if (cli.hasOption(ARG_GREATER_MANCHESTER_EXTENSIONS)) {
      GreaterManchesterParser parser = new GreaterManchesterParser();
      converter.getParser().addExtension("ZA", parser);
    }
  }

  protected void buildOptions(Options options) {
    options.addOption(ARG_AGENCY_ID, true, "agency id");
    options.addOption(ARG_AGENCY_LANG, true, "agency lang");
    options.addOption(ARG_AGENCY_NAME, true, "agency name");
    options.addOption(ARG_AGENCY_PHONE, true, "agency phone");
    options.addOption(ARG_AGENCY_TIMEZONE, true, "agency timezone");
    options.addOption(ARG_AGENCY_URL, true, "agency url");
    options.addOption(ARG_VEHICLE_TYPE, true, "vehicle type");
    options.addOption(ARG_PRUNE_STOPS_WITH_NO_LOCATION_INFO, false, "");
    options.addOption(ARG_PRUNE_STOPS_WITH_PREFIX, true, "");
    options.addOption(ARG_PRUNE_TRIPS_WITH_MISSING_STOPS, false, "");
    options.addOption(ARG_LOCATION_SCALE_FACTOR, true, "");
    options.addOption(ARG_PREFERRED_DIRECTION_IDS_FOR_ROUTE_DETAILs, true, "");
    options.addOption(ARG_NAPTAN_CSV_PATH, true, "");
    options.addOption(ARG_NATIONAL_EXPRESS_EXTENSIONS, false, "");
    options.addOption(ARG_GREATER_MANCHESTER_EXTENSIONS, false, "");
  }

  private void usage() throws IOException {
    InputStream in = getClass().getResourceAsStream("usage.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line = null;
    while ((line = reader.readLine()) != null) {
      System.err.println(line);
    }
    reader.close();
  }
}
