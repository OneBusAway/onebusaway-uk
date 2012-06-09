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

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

/**
 * @author bdferris
 * 
 */
public class BasicScheduleElement extends CifElement {

  public enum EBankHolidayRunning {
    BANK_HOLIDAY_DATES, EDINBURGH_HOLIDAY_DATES, GLASGOW_HOLIDAY_DATES
  }

  public enum EStatus {
    BUS, FREIGHT, PASSENGER_AND_PARCELS, SHIP, TRIP, STP_PASSENGER_AND_PARCELS, STP_FREIGHT, STP_TRIP, STP_SHIP, STP_BUS
  }

  private ETransactionType transactionType;

  private String trainUid;

  private Date runsFrom;

  private Date runsTo;

  private EnumSet<EDays> days;

  private EBankHolidayRunning bankHolidayDates;

  private EStatus status;

  private String identity;

  private String headcode;

  private String serviceCode;

  private EStpIndicator stpIndicator;

  private List<TimepointElement> timepoints = new ArrayList<TimepointElement>();

  public BasicScheduleElement() {
    super(CifElement.Type.BASIC_SCHEDULE);
  }

  public ETransactionType getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(ETransactionType transactionType) {
    this.transactionType = transactionType;
  }

  public String getTrainUid() {
    return trainUid;
  }

  public void setTrainUid(String trainUid) {
    this.trainUid = trainUid;
  }

  public Date getRunsFrom() {
    return runsFrom;
  }

  public void setRunsFrom(Date runsFrom) {
    this.runsFrom = runsFrom;
  }

  public Date getRunsTo() {
    return runsTo;
  }

  public void setRunsTo(Date runsTo) {
    this.runsTo = runsTo;
  }

  public EnumSet<EDays> getDays() {
    return days;
  }

  public void setDays(EnumSet<EDays> days) {
    this.days = days;
  }

  public EBankHolidayRunning getBankHolidayDates() {
    return bankHolidayDates;
  }

  public void setBankHolidayDates(EBankHolidayRunning bankHolidayDates) {
    this.bankHolidayDates = bankHolidayDates;
  }

  public EStatus getStatus() {
    return status;
  }

  public void setStatus(EStatus status) {
    this.status = status;
  }

  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String identity) {
    this.identity = identity;
  }

  public String getHeadcode() {
    return headcode;
  }

  public void setHeadcode(String headcode) {
    this.headcode = headcode;
  }

  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public EStpIndicator getStpIndicator() {
    return stpIndicator;
  }

  public void setStpIndicator(EStpIndicator stpIndicator) {
    this.stpIndicator = stpIndicator;
  }

  public List<TimepointElement> getTimepoints() {
    return timepoints;
  }

  public void setTimepoints(List<TimepointElement> timepoints) {
    this.timepoints = timepoints;
  }
}
