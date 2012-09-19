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
package org.onebusaway.uk.naptan.csv;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.ListEntityHandler;
import org.onebusaway.csv_entities.schema.AnnotationDrivenEntitySchemaFactory;

public class NaPTANStopTest {

  @Test
  public void test() throws IOException {

    CsvEntityReader reader = new CsvEntityReader();
    reader.setInputLocation(new File(
        "src/test/resources/org/onebusaway/uk/naptan/csv/test_data"));
    AnnotationDrivenEntitySchemaFactory entitySchema = new AnnotationDrivenEntitySchemaFactory();
    entitySchema.addPackageToScan("org.onebusaway.uk.naptan.csv");
    reader.setEntitySchemaFactory(entitySchema);
    ListEntityHandler<NaPTANStop> stopHandler = new ListEntityHandler<NaPTANStop>();
    reader.addEntityHandler(stopHandler);
    reader.readEntities(NaPTANStop.class);
    reader.close();
    List<NaPTANStop> stops = stopHandler.getValues();
    assertEquals(2, stops.size());
    NaPTANStop stop = stops.get(0);
    assertEquals("010000001", stop.getAtcoCode());
    assertEquals("bstpgit", stop.getNaptanCode());
    assertEquals("Cassell Road", stop.getCommonName());
    assertEquals(-2.51685, stop.getLongitude(), 1e-5);
    assertEquals(51.48441, stop.getLatitude(), 1e-5);
  }
}
