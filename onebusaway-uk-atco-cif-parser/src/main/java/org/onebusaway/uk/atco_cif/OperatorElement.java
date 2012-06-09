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

public class OperatorElement extends AtcoCifElement {

  private String operatorId;

  private String shortFormName;

  private String legalName;

  private String enquiryPhone;

  private String contactPhone;

  public OperatorElement() {
    super(Type.OPERATOR);
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

  public String getShortFormName() {
    return shortFormName;
  }

  public void setShortFormName(String shortFormName) {
    this.shortFormName = shortFormName;
  }

  public String getLegalName() {
    return legalName;
  }

  public void setLegalName(String legalName) {
    this.legalName = legalName;
  }

  public String getEnquiryPhone() {
    return enquiryPhone;
  }

  public void setEnquiryPhone(String enquiryPhone) {
    this.enquiryPhone = enquiryPhone;
  }

  public String getContactPhone() {
    return contactPhone;
  }

  public void setContactPhone(String contactPhone) {
    this.contactPhone = contactPhone;
  }
}
