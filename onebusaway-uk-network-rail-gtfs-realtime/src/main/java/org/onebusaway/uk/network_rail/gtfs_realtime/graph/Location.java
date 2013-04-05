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

import java.awt.geom.Point2D;

import org.onebusaway.uk.parser.ProjectionSupport;

class Location {
  boolean fixed;
  double x;
  double y;
  double lat;
  double lon;

  public Location() {

  }

  public Location(java.awt.geom.Point2D.Double point) {
    this.x = point.x;
    this.y = point.y;
    Point2D.Double latLon = ProjectionSupport.convertToLatLon(point.x, point.y);
    this.lat = latLon.y;
    this.lon = latLon.x;
  }

  public Point2D.Double getPoint() {
    return new Point2D.Double(x, y);
  }

  @Override
  public String toString() {
    return lat + " " + lon;
  }

  public double getDistance(Location p) {
    double dx = x - p.x;
    double dy = y - p.y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}