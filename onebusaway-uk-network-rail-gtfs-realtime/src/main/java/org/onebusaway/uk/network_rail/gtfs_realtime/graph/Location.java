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