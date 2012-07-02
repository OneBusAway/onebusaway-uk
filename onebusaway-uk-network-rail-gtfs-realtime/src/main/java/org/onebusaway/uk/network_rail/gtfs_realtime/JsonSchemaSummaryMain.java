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
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Give a set of log file of JSON messages with unknown structure, dump a
 * summary of the schema of these messages.
 * 
 * @author bdferris
 * 
 */
public class JsonSchemaSummaryMain {
  public static void main(String[] args) throws JsonSyntaxException,
      IOException {

    List<File> files = new ArrayList<File>();
    for (String arg : args) {
      getLogFiles(new File(arg), files);
    }

    Map<String, Object> structure = new TreeMap<String, Object>();

    for (File file : files) {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line = null;
      while ((line = reader.readLine()) != null) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(line);

        processElement(element, structure);
      }
    }

    print(structure, "");
  }

  private static void processElement(JsonElement element,
      Map<String, Object> structure) {
    if (element.isJsonArray()) {
      Map<String, Object> subStructure = extend(structure, "[]");
      JsonArray array = element.getAsJsonArray();
      for (int i = 0; i < array.size(); ++i) {
        processElement(array.get(i), subStructure);
      }
    } else if (element.isJsonObject()) {
      JsonObject object = element.getAsJsonObject();
      for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
        processElement(entry.getValue(), extend(structure, entry.getKey()));
      }
    }
  }

  private static Map<String, Object> extend(Map<String, Object> structure,
      String key) {
    @SuppressWarnings("unchecked")
    Map<String, Object> subStructure = (Map<String, Object>) structure.get(key);
    if (subStructure == null) {
      subStructure = new TreeMap<String, Object>();
      structure.put(key, subStructure);
    }
    return subStructure;
  }

  private static void getLogFiles(File logDir, List<File> matchingFiles) {
    if (logDir.isDirectory()) {
      for (File childDir : logDir.listFiles()) {
        getLogFiles(childDir, matchingFiles);
      }
    } else if (logDir.getName().endsWith("-TD.json")) {
      matchingFiles.add(logDir);
    }
  }

  private static void print(Map<String, Object> structure, String prefix) {
    for (Map.Entry<String, Object> entry : structure.entrySet()) {
      System.out.print(prefix + entry.getKey() + " = {");
      @SuppressWarnings("unchecked")
      Map<String, Object> subStructure = (Map<String, Object>) entry.getValue();
      if (!subStructure.isEmpty()) {
        System.out.println();
        print(subStructure, prefix + "  ");
      }
      System.out.println(prefix + "},");
    }
  }

}
