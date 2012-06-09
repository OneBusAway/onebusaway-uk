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
package org.onebusaway.uk.atco_cif;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JourneyHeaderElement extends AtcoCifElement {

  private String operatorId;

  private String journeyIdentifier;

  private Date startDate;

  private Date endDate;

  private int monday;

  private int tuesday;

  private int wednesday;

  private int thursday;

  private int friday;

  private int saturday;

  private int sunday;

  private String routeIdentifier;

  private String routeDirection;

  private String vehicleType;

  private List<JourneyDateRunningElement> calendarModifications = new ArrayList<JourneyDateRunningElement>();

  private List<JourneyTimePointElement> timePoints = new ArrayList<JourneyTimePointElement>();

  public JourneyHeaderElement() {
    super(Type.JOURNEY_HEADER);
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getJourneyIdentifier() {
    return journeyIdentifier;
  }

  public void setJourneyIdentifier(String journeyIdentifier) {
    this.journeyIdentifier = journeyIdentifier;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  public int getMonday() {
    return monday;
  }

  public void setMonday(int monday) {
    this.monday = monday;
  }

  public int getTuesday() {
    return tuesday;
  }

  public void setTuesday(int tuesday) {
    this.tuesday = tuesday;
  }

  public int getWednesday() {
    return wednesday;
  }

  public void setWednesday(int wednesday) {
    this.wednesday = wednesday;
  }

  public int getThursday() {
    return thursday;
  }

  public void setThursday(int thursday) {
    this.thursday = thursday;
  }

  public int getFriday() {
    return friday;
  }

  public void setFriday(int friday) {
    this.friday = friday;
  }

  public int getSaturday() {
    return saturday;
  }

  public void setSaturday(int saturday) {
    this.saturday = saturday;
  }

  public int getSunday() {
    return sunday;
  }

  public void setSunday(int sunday) {
    this.sunday = sunday;
  }

  public String getRouteIdentifier() {
    return routeIdentifier;
  }

  public void setRouteIdentifier(String routeIdentifier) {
    this.routeIdentifier = routeIdentifier;
  }

  public String getRouteDirection() {
    return routeDirection;
  }

  public void setRouteDirection(String routeDirection) {
    this.routeDirection = routeDirection;
  }

  public String getVehicleType() {
    return vehicleType;
  }

  public void setVehicleType(String vehicleType) {
    this.vehicleType = vehicleType;
  }

  public List<JourneyDateRunningElement> getCalendarModifications() {
    return calendarModifications;
  }

  public void setCalendarModifications(
      List<JourneyDateRunningElement> calendarModifications) {
    this.calendarModifications = calendarModifications;
  }

  public List<JourneyTimePointElement> getTimePoints() {
    return timePoints;
  }

  public void setTimePoints(List<JourneyTimePointElement> elements) {
    this.timePoints = elements;
  }
}
