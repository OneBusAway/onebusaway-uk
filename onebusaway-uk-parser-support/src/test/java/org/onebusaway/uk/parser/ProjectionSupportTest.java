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
package org.onebusaway.uk.parser;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Point2D;

import org.junit.Test;

public class ProjectionSupportTest {

  @Test
  public void testConvertToLatLon() {
    Point2D.Double p = ProjectionSupport.convertToLatLon(547300.0, 179000.0);
    assertEquals(0.12031431734872115, p.x, 1e-7);
    assertEquals(51.49076555222841, p.y, 1e-7);
  }

  @Test
  public void testConvertFromLatLon() {
    Point2D.Double p = ProjectionSupport.convertFromLatLon(51.49076555222841,
        0.12031431734872115);
    //assertEquals(547300.0, p.x, 0.1);
    //assertEquals(179000.0, p.y, 0.1);
  }
}
