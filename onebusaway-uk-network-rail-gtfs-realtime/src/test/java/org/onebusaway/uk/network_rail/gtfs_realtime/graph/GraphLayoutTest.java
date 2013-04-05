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
package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class GraphLayoutTest {

  private GraphLayout layout_ = new GraphLayout();

  @Test
  public void testA() {
    Location a = new Location();
    a.x = 0;
    a.y = 0;
    Location b = new Location();
    b.x = 1;
    b.y = 0;
    Map<Location, Integer> weights = new HashMap<Location, Integer>();
    weights.put(a, 1);
    weights.put(b, 3);

    Location c = new Location();
    c.x = 0.5;
    c.y = 0;
    layout_.updateLocation(weights, c, 0.001);

    assertEquals(0.25, c.x, 0.001);
    assertEquals(0.00, c.y, 0.001);
  }

  @Test
  public void testB() {
    Location a = new Location();
    a.x = 1;
    a.y = 1;
    Location b = new Location();
    b.x = 9;
    b.y = 5;
    Map<Location, Integer> weights = new HashMap<Location, Integer>();
    weights.put(a, 1);
    weights.put(b, 3);

    Location c = new Location();
    c.x = 1.2;
    c.y = 1.2;
    layout_.updateLocation(weights, c, 0.001);

    assertEquals(3.0, c.x, 0.01);
    assertEquals(2.0, c.y, 0.01
        );
  }
}
