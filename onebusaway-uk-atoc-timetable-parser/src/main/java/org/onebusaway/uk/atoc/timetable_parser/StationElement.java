/**
 * Copyright (C) 2012 Google, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.uk.atoc.timetable_parser;

import org.onebusaway.uk.parser.Element;

public class StationElement extends Element {

  public enum ECategory {
    NOT_AN_INTERCHANGE, SMALL_INTERCHANGE, MEDIUM_INTERCHANGE, LARGE_INTERCHANGE, SUBSIDIARY
  }

  private String name;

  private ECategory category;

  private String tiploc;

  private String subsidiaryAlphaCode;

  private String alphaCode;

  private double northing;

  private double easting;

  private double lat;

  private double lon;

  private int changeTime;

  private String footnote;

  private String region;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ECategory getCategory() {
    return category;
  }

  public void setCategory(ECategory category) {
    this.category = category;
  }

  public String getTiploc() {
    return tiploc;
  }

  public void setTiploc(String tiploc) {
    this.tiploc = tiploc;
  }

  public String getSubsidiaryAlphaCode() {
    return subsidiaryAlphaCode;
  }

  public void setSubsidiaryAlphaCode(String subsidiaryAlphaCode) {
    this.subsidiaryAlphaCode = subsidiaryAlphaCode;
  }

  public String getAlphaCode() {
    return alphaCode;
  }

  public void setAlphaCode(String alphaCode) {
    this.alphaCode = alphaCode;
  }

  public double getNorthing() {
    return northing;
  }

  public void setNorthing(double northing) {
    this.northing = northing;
  }

  public double getEasting() {
    return easting;
  }

  public void setEasting(double easting) {
    this.easting = easting;
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

  public int getChangeTime() {
    return changeTime;
  }

  public void setChangeTime(int changeTime) {
    this.changeTime = changeTime;
  }

  public String getFootnote() {
    return footnote;
  }

  public void setFootnote(String footnote) {
    this.footnote = footnote;
  }

  public String getRegion() {
    return region;
  }

  public void setRegion(String region) {
    this.region = region;
  }
}
