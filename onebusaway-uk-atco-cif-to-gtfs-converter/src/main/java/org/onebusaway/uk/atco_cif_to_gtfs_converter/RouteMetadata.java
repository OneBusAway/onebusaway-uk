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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.onebusaway.uk.atco_cif.RouteDescriptionElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteMetadata {

  private static final Logger _log = LoggerFactory.getLogger(RouteMetadata.class);

  private Set<String> _directions = new HashSet<String>();

  private Map<String, RouteDescriptionElement> _routeDescriptionsByDirection = new HashMap<String, RouteDescriptionElement>();

  private Map<String, Integer> _directionIdsByDirection = null;

  public void addDirection(String routeDirection) {
    _directions.add(routeDirection);
  }

  public RouteDescriptionElement getRouteDescriptionForDirection(
      String direction) {
    return _routeDescriptionsByDirection.get(direction);
  }

  public void addRouteDescription(RouteDescriptionElement route) {
    RouteDescriptionElement existing = _routeDescriptionsByDirection.put(
        route.getRouteDescription(), route);
    if (existing != null) {
      _log.warn("multiple route descriptions with the same direction");
    }
  }

  public Integer getDirectionIdForDirection(String routeDirection) {
    if (_directionIdsByDirection == null) {
      _directionIdsByDirection = new HashMap<String, Integer>();
      if (_directions.size() <= 2) {
        for (String direction : _directions) {
          _directionIdsByDirection.put(direction,
              _directionIdsByDirection.size());
        }
      }
    }
    return _directionIdsByDirection.get(routeDirection);
  }

}
