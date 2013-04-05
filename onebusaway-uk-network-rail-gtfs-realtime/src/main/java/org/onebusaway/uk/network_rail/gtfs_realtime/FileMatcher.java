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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileMatcher {

  private String _extension;

  private long _minLastModifiedTime = 0;

  private String _contains;

  public void setExtension(String extension) {
    _extension = extension;
  }

  public void setMinLastModifiedTime(long minLastModifiedTime) {
    _minLastModifiedTime = minLastModifiedTime;
  }

  public void setContains(String contains) {
    _contains = contains;
  }

  public List<File> matchFiles(File file) {
    List<File> matchingFiles = new ArrayList<File>();
    matchFiles(file, matchingFiles);
    return matchingFiles;
  }

  public void matchFiles(File file, List<File> matchingFiles) {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        matchFiles(child, matchingFiles);
      }
    } else {
      if (_extension != null && !file.getName().endsWith(_extension)) {
        return;
      }
      if (_contains != null && !file.getAbsolutePath().contains(_contains)) {
        return;
      }
      if (_minLastModifiedTime != 0
          && _minLastModifiedTime > file.lastModified()) {
        return;
      }
      matchingFiles.add(file);
    }
  }
}
