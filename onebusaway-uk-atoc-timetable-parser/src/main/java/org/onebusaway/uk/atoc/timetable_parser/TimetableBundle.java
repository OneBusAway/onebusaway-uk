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
package org.onebusaway.uk.atoc.timetable_parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.onebusaway.uk.network_rail.cif.CifParser;
import org.onebusaway.uk.parser.ContentHandler;

public class TimetableBundle {

  private final FileFinder _files;

  public TimetableBundle(File path) {
    if (path.isDirectory()) {
      _files = new DirectoryFileFinder(path);
    } else {
      throw new UnsupportedOperationException("could not process bundle path: "
          + path);
    }
  }

  public void readTimetable(ContentHandler handler) throws IOException {
    CifParser parser = new CifParser();
    for (FileEntry entry : _files.find(".MCA")) {
      parser.parse(entry.getPath(), entry.open(), handler);
    }
  }

  public void readMasterStationNames(ContentHandler handler) throws IOException {
    MasterStationsNamesParser parser = new MasterStationsNamesParser();
    for (FileEntry entry : _files.find(".MSN")) {
      parser.parse(entry.getPath(), entry.open(), handler);
    }
  }

  /****
   * 
   ****/

  private interface FileEntry {
    public String getPath();

    public BufferedReader open() throws IOException;
  }

  private interface FileFinder {
    public Iterable<FileEntry> find(String extension);
  }

  private static class DirectoryFileFinder implements FileFinder {

    private final File _path;

    public DirectoryFileFinder(File path) {
      _path = path;
    }

    @Override
    public Iterable<FileEntry> find(final String extension) {
      File[] files = _path.listFiles(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return name.endsWith(extension);
        }
      });

      List<FileEntry> entries = new ArrayList<FileEntry>();
      for (File file : files) {
        entries.add(new FileEntryImpl(file));
      }
      return entries;
    }
  }

  private static class FileEntryImpl implements FileEntry {

    private File _path;

    public FileEntryImpl(File path) {
      _path = path;
    }

    @Override
    public String getPath() {
      return _path.getAbsolutePath();
    }

    @Override
    public BufferedReader open() throws IOException {
      return new BufferedReader(new InputStreamReader(
          new FileInputStream(_path), "UTF-8"));
    }
  }
}
