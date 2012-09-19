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

import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.csv_entities.schema.annotations.CsvFieldNameConvention;
import org.onebusaway.csv_entities.schema.annotations.CsvFields;

@CsvFields(filename = "Stops.csv", fieldNameConvention = CsvFieldNameConvention.CAPITALIZED_CAMEL_CASE)
public class NaPTANStop {
  private String atcoCode;
  @CsvField(optional = true)
  private String naptanCode;
  @CsvField(optional = true)
  private String plateCode;
  @CsvField(optional = true)
  private String cleardownCode;
  private String commonName;
  @CsvField(optional = true)
  private String commonNameLang;
  @CsvField(optional = true)
  private String shortCommonName;
  @CsvField(optional = true)
  private String shortCommonLongName;
  @CsvField(optional = true)
  private String landmark;
  @CsvField(optional = true)
  private String landmarkLang;
  @CsvField(optional = true)
  private String street;
  @CsvField(optional = true)
  private String streetLang;
  @CsvField(optional = true)
  private String crossing;
  @CsvField(optional = true)
  private String crossingLang;
  @CsvField(optional = true)
  private String indicator;
  @CsvField(optional = true)
  private String indicatorLang;
  @CsvField(optional = true)
  private String bearing;
  private String nptgLocalityCode;
  @CsvField(optional = true)
  private String localityName;
  @CsvField(optional = true)
  private String parentLocalityName;
  @CsvField(optional = true)
  private String grandParentLocalityName;
  @CsvField(optional = true)
  private String town;
  @CsvField(optional = true)
  private String townLang;
  @CsvField(optional = true)
  private String suburb;
  @CsvField(optional = true)
  private String suburbLang;
  @CsvField(optional = true)
  private String localityCentre;
  private String gridType;
  private double easting;
  private double northing;
  private double longitude;
  private double latitude;
  private String stopType;
  @CsvField(optional = true)
  private String busStopType;
  @CsvField(optional = true)
  private String timingStatus;
  @CsvField(optional = true)
  private String defaultWaitTime;
  @CsvField(optional = true)
  private String notes;
  @CsvField(optional = true)
  private String notesLang;
  private String administrativeAreaCode;
  private String creationDateTime;
  @CsvField(optional = true)
  private String modificationDateTime;
  @CsvField(optional = true)
  private String revisionNumber;
  private String modification;
  private String status;

  public String getAtcoCode() {
    return atcoCode;
  }

  public void setAtcoCode(String atcoCode) {
    this.atcoCode = atcoCode;
  }

  public String getNaptanCode() {
    return naptanCode;
  }

  public void setNaptanCode(String naptanCode) {
    this.naptanCode = naptanCode;
  }

  public String getPlateCode() {
    return plateCode;
  }

  public void setPlateCode(String plateCode) {
    this.plateCode = plateCode;
  }

  public String getCleardownCode() {
    return cleardownCode;
  }

  public void setCleardownCode(String cleardownCode) {
    this.cleardownCode = cleardownCode;
  }

  public String getCommonName() {
    return commonName;
  }

  public void setCommonName(String commonName) {
    this.commonName = commonName;
  }

  public String getCommonNameLang() {
    return commonNameLang;
  }

  public void setCommonNameLang(String commonNameLang) {
    this.commonNameLang = commonNameLang;
  }

  public String getShortCommonName() {
    return shortCommonName;
  }

  public void setShortCommonName(String shortCommonName) {
    this.shortCommonName = shortCommonName;
  }

  public String getShortCommonLongName() {
    return shortCommonLongName;
  }

  public void setShortCommonLongName(String shortCommonLongName) {
    this.shortCommonLongName = shortCommonLongName;
  }

  public String getLandmark() {
    return landmark;
  }

  public void setLandmark(String landmark) {
    this.landmark = landmark;
  }

  public String getLandmarkLang() {
    return landmarkLang;
  }

  public void setLandmarkLang(String landmarkLang) {
    this.landmarkLang = landmarkLang;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getStreetLang() {
    return streetLang;
  }

  public void setStreetLang(String streetLang) {
    this.streetLang = streetLang;
  }

  public String getCrossing() {
    return crossing;
  }

  public void setCrossing(String crossing) {
    this.crossing = crossing;
  }

  public String getCrossingLang() {
    return crossingLang;
  }

  public void setCrossingLang(String crossingLang) {
    this.crossingLang = crossingLang;
  }

  public String getIndicator() {
    return indicator;
  }

  public void setIndicator(String indicator) {
    this.indicator = indicator;
  }

  public String getIndicatorLang() {
    return indicatorLang;
  }

  public void setIndicatorLang(String indicatorLang) {
    this.indicatorLang = indicatorLang;
  }

  public String getBearing() {
    return bearing;
  }

  public void setBearing(String bearing) {
    this.bearing = bearing;
  }

  public String getNptgLocalityCode() {
    return nptgLocalityCode;
  }

  public void setNptgLocalityCode(String nptgLocalityCode) {
    this.nptgLocalityCode = nptgLocalityCode;
  }

  public String getLocalityName() {
    return localityName;
  }

  public void setLocalityName(String localityName) {
    this.localityName = localityName;
  }

  public String getParentLocalityName() {
    return parentLocalityName;
  }

  public void setParentLocalityName(String parentLocalityName) {
    this.parentLocalityName = parentLocalityName;
  }

  public String getGrandParentLocalityName() {
    return grandParentLocalityName;
  }

  public void setGrandParentLocalityName(String grandParentLocalityName) {
    this.grandParentLocalityName = grandParentLocalityName;
  }

  public String getTown() {
    return town;
  }

  public void setTown(String town) {
    this.town = town;
  }

  public String getTownLang() {
    return townLang;
  }

  public void setTownLang(String townLang) {
    this.townLang = townLang;
  }

  public String getSuburb() {
    return suburb;
  }

  public void setSuburb(String suburb) {
    this.suburb = suburb;
  }

  public String getSuburbLang() {
    return suburbLang;
  }

  public void setSuburbLang(String suburbLang) {
    this.suburbLang = suburbLang;
  }

  public String getLocalityCentre() {
    return localityCentre;
  }

  public void setLocalityCentre(String localityCentre) {
    this.localityCentre = localityCentre;
  }

  public String getGridType() {
    return gridType;
  }

  public void setGridType(String gridType) {
    this.gridType = gridType;
  }

  public double getEasting() {
    return easting;
  }

  public void setEasting(double easting) {
    this.easting = easting;
  }

  public double getNorthing() {
    return northing;
  }

  public void setNorthing(double northing) {
    this.northing = northing;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public String getStopType() {
    return stopType;
  }

  public void setStopType(String stopType) {
    this.stopType = stopType;
  }

  public String getBusStopType() {
    return busStopType;
  }

  public void setBusStopType(String busStopType) {
    this.busStopType = busStopType;
  }

  public String getTimingStatus() {
    return timingStatus;
  }

  public void setTimingStatus(String timingStatus) {
    this.timingStatus = timingStatus;
  }

  public String getDefaultWaitTime() {
    return defaultWaitTime;
  }

  public void setDefaultWaitTime(String defaultWaitTime) {
    this.defaultWaitTime = defaultWaitTime;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getNotesLang() {
    return notesLang;
  }

  public void setNotesLang(String notesLang) {
    this.notesLang = notesLang;
  }

  public String getAdministrativeAreaCode() {
    return administrativeAreaCode;
  }

  public void setAdministrativeAreaCode(String administrativeAreaCode) {
    this.administrativeAreaCode = administrativeAreaCode;
  }

  public String getCreationDateTime() {
    return creationDateTime;
  }

  public void setCreationDateTime(String creationDateTime) {
    this.creationDateTime = creationDateTime;
  }

  public String getModificationDateTime() {
    return modificationDateTime;
  }

  public void setModificationDateTime(String modificationDateTime) {
    this.modificationDateTime = modificationDateTime;
  }

  public String getRevisionNumber() {
    return revisionNumber;
  }

  public void setRevisionNumber(String revisionNumber) {
    this.revisionNumber = revisionNumber;
  }

  public String getModification() {
    return modification;
  }

  public void setModification(String modification) {
    this.modification = modification;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
