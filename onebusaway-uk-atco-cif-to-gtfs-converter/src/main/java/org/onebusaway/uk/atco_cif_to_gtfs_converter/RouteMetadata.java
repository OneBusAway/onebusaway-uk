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
import org.onebusaway.uk.atco_cif.extensions.NationalExpressRouteDetailsElement;

public class RouteMetadata {

  private Set<String> _directions = new HashSet<String>();

  private Map<String, RouteDescriptionElement> _routeDescriptionsByDirection = new HashMap<String, RouteDescriptionElement>();

  private Map<String, Integer> _directionIdsByDirection = null;

  private Map<String, String> _routeLongNamesByDirectionId = new HashMap<String, String>();

  private Map<String, String> _routeUrlsByDirectionId = new HashMap<String, String>();

  private Map<String, String> _directionNamesByDirectionId = new HashMap<String, String>();

  public void addDirection(String routeDirection) {
    _directions.add(routeDirection);
  }

  public RouteDescriptionElement getRouteDescriptionForDirection(
      String direction) {
    return _routeDescriptionsByDirection.get(direction);
  }

  public String getRouteLongNameForDirectionId(String directionId) {
    return _routeLongNamesByDirectionId.get(directionId);
  }

  public String getRouteUrlForDirectionId(String directionId) {
    return _routeUrlsByDirectionId.get(directionId);
  }

  public String getDirectionNameForDirectionId(String directionId) {
    return _directionNamesByDirectionId.get(directionId);
  }

  public void addRouteDescription(RouteDescriptionElement route) {
    if (!isEmpty(route.getRouteDescription())) {
      _routeLongNamesByDirectionId.put(route.getRouteDirection(),
          route.getRouteDescription());
      _directionNamesByDirectionId.put(route.getRouteDirection(),
          route.getRouteDescription());
    }
  }

  public void addNXRouteDetails(NationalExpressRouteDetailsElement element) {
    if (!isEmpty(element.getRouteShortName())) {
      _directionNamesByDirectionId.put(element.getDirectionId(),
          element.getRouteShortName());
    }
    if (!isEmpty(element.getRouteUrl())) {
      _routeUrlsByDirectionId.put(element.getDirectionId(),
          element.getRouteUrl());
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

  private static final boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
