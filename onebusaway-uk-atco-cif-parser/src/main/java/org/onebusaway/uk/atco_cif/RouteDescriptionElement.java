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

public class RouteDescriptionElement extends AtcoCifElement {

  private String operatorId;
  private String routeNumber;
  private String routeDirection;
  private String routeDescription;

  public RouteDescriptionElement() {
    super(Type.ROUTE_DESCRIPTION);
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getRouteNumber() {
    return routeNumber;
  }

  public void setRouteNumber(String routeNumber) {
    this.routeNumber = routeNumber;
  }

  public String getRouteDirection() {
    return routeDirection;
  }

  public void setRouteDirection(String routeDirection) {
    this.routeDirection = routeDirection;
  }

  public String getRouteDescription() {
    return routeDescription;
  }

  public void setRouteDescription(String routeDescription) {
    this.routeDescription = routeDescription;
  }
}
