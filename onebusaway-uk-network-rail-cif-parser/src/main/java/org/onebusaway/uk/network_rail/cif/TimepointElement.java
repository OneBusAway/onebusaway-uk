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
package org.onebusaway.uk.network_rail.cif;

public abstract class TimepointElement extends CifElement implements
    ScheduleChildElement {

  public TimepointElement(Type type) {
    super(type);
  }

  private BasicScheduleElement schedule;

  private String tiploc;

  private String tiplocSuffix;

  private int scheduledArrivalTime;

  private int publicArrivalTime;

  private int scheduledDepartureTime;

  private int publicDepartureTime;

  private String platform;

  private String line;

  private String path;

  private String activity;

  private int engineeringAllowance;

  private int pathingAllowance;

  private int performanceAllowance;

  public BasicScheduleElement getSchedule() {
    return schedule;
  }

  public void setSchedule(BasicScheduleElement schedule) {
    this.schedule = schedule;
  }

  public String getTiploc() {
    return tiploc;
  }

  public void setTiploc(String tiploc) {
    this.tiploc = tiploc;
  }

  public String getTiplocSuffix() {
    return tiplocSuffix;
  }

  public void setTiplocSuffix(String tiplocSuffix) {
    this.tiplocSuffix = tiplocSuffix;
  }

  public int getScheduledArrivalTime() {
    return scheduledArrivalTime;
  }

  public void setScheduledArrivalTime(int scheduledArrivalTime) {
    this.scheduledArrivalTime = scheduledArrivalTime;
  }

  public int getPublicArrivalTime() {
    return publicArrivalTime;
  }

  public void setPublicArrivalTime(int publicArrivalTime) {
    this.publicArrivalTime = publicArrivalTime;
  }

  public int getScheduledDepartureTime() {
    return scheduledDepartureTime;
  }

  public void setScheduledDepartureTime(int scheduledDepartureTime) {
    this.scheduledDepartureTime = scheduledDepartureTime;
  }

  public int getPublicDepartureTime() {
    return publicDepartureTime;
  }

  public void setPublicDepartureTime(int publicDepartureTime) {
    this.publicDepartureTime = publicDepartureTime;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getLine() {
    return line;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setLine(String line) {
    this.line = line;
  }

  public String getActivity() {
    return activity;
  }

  public void setActivity(String activity) {
    this.activity = activity;
  }

  public int getEngineeringAllowance() {
    return engineeringAllowance;
  }

  public void setEngineeringAllowance(int engineeringAllowance) {
    this.engineeringAllowance = engineeringAllowance;
  }

  public int getPathingAllowance() {
    return pathingAllowance;
  }

  public void setPathingAllowance(int pathingAllowance) {
    this.pathingAllowance = pathingAllowance;
  }

  public int getPerformanceAllowance() {
    return performanceAllowance;
  }

  public void setPerformanceAllowance(int performanceAllowance) {
    this.performanceAllowance = performanceAllowance;
  }

  @Override
  public String toString() {
    return tiploc +":" + publicArrivalTime;
  }
}
