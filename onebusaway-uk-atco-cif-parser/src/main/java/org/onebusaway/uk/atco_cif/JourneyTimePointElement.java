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

public abstract class JourneyTimePointElement extends AtcoCifElement implements
    JourneyChildElement {

  private JourneyHeaderElement header;

  private String locationId;

  public JourneyTimePointElement(AtcoCifElement.Type type) {
    super(type);
  }

  public JourneyHeaderElement getHeader() {
    return header;
  }

  public void setHeader(JourneyHeaderElement header) {
    this.header = header;
  }

  public String getLocationId() {
    return locationId;
  }

  public void setLocationId(String locationId) {
    this.locationId = locationId;
  }

  public abstract int getArrivalTime();

  public abstract int getDepartureTime();

  public abstract void setArrivalTime(int arrivalTime);

  public abstract void setDepartureTime(int departureTime);
  
  public boolean isPickUpAllowed() {
    return true;
  }
  
  public boolean isDropOffAllowed() {
    return true;
  }

  @Override
  public String toString() {
    return getArrivalTime() + " " + getDepartureTime();
  }
}
