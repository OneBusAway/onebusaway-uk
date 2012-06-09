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

import java.util.Date;

public class JourneyDateRunningElement extends AtcoCifElement implements
    JourneyChildElement, Comparable<JourneyDateRunningElement> {

  private Date startDate;

  private Date endDate;

  private int operationCode;

  public JourneyDateRunningElement() {
    super(Type.JOURNEY_DATE_RUNNING);
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

  public int getOperationCode() {
    return operationCode;
  }

  public void setOperationCode(int operationCode) {
    this.operationCode = operationCode;
  }

  @Override
  public int compareTo(JourneyDateRunningElement o) {
    int c = this.startDate.compareTo(o.startDate);
    if (c != 0)
      return c;
    c = this.endDate.compareTo(o.endDate);
    if (c != 0)
      return c;
    return o.operationCode - this.operationCode;
  }
}
