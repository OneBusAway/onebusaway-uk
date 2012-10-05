package org.onebusaway.uk.network_rail.gtfs_realtime.model;

import org.onebusaway.csv_entities.schema.annotations.CsvField;

public class StationLocation {
  private String tiploc;
  private double lat;
  private double lon;
  
  @CsvField(optional=true)
  private double x;
  
  @CsvField(optional=true)
  private double y;

  public String getTiploc() {
    return tiploc;
  }

  public void setTiploc(String tiploc) {
    this.tiploc = tiploc;
  }

  public double getLat() {
    return lat;
  }

  public void setLat(double lat) {
    this.lat = lat;
  }

  public double getLon() {
    return lon;
  }

  public void setLon(double lon) {
    this.lon = lon;
  }

  public double getX() {
    return x;
  }

  public void setX(double x) {
    this.x = x;
  }

  public double getY() {
    return y;
  }

  public void setY(double y) {
    this.y = y;
  }
}
