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

public class JourneyOriginElement extends JourneyTimePointElement {

  private int departureTime;

  public JourneyOriginElement() {
    super(AtcoCifElement.Type.JOURNEY_ORIGIN);
  }

  @Override
  public int getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(int departureTime) {
    this.departureTime = departureTime;
  }

  @Override
  public int getArrivalTime() {
    return getDepartureTime();
  }

  @Override
  public void setArrivalTime(int arrivalTime) {
    // IGNORE
  }
}
