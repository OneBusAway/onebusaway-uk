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
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;

public class ReplayMain {

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private Processor _processor = new Processor();

  public static void main(String[] args) throws IOException, ParseException {
    ReplayMain m = new ReplayMain();
    m.run(args);
  }

  private void run(String[] args) throws IOException, ParseException {
    Options options = new Options();
    buildOptions(options);
    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    _processor.setAtocTimetablePath(new File(
        cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));
    _processor.start();

    List<File> files = new ArrayList<File>();
    for (String path : cli.getArgs()) {
      collectFiles(new File(path), files);
    }

    for (File file : files) {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        if (!line.startsWith("[")) {
          continue;
        }
        _processor.processMessages(line);
      }
      reader.close();
    }
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
  }

  private List<File> collectFiles(File path, List<File> matches) {
    if (path.isDirectory()) {
      for (File child : path.listFiles()) {
        collectFiles(child, matches);
      }
    } else if (path.getName().endsWith(".json")) {
      matches.add(path);
    }
    return matches;
  }
}
