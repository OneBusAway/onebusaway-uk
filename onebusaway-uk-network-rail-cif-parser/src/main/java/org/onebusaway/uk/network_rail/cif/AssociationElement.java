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

import java.util.Date;
import java.util.EnumSet;

/**
 * @author bdferris
 * 
 */
public class AssociationElement extends CifElement {

  public enum ECategory {
    JOIN, DIVIDE, NEXT
  }

  public enum EDateIndicator {
    STANDARD, OVER_NEXT_MIDNIGHT, OVER_PREVIOUS_MIDNIGHT
  }

  public enum EAssociationType {
    PASSENGER, OPERATING
  }

  private ETransactionType transactionType;

  private String trainUid;

  private String associatedTrainUid;

  private Date startDate;

  private Date endDate;

  private EnumSet<EDays> days = EnumSet.noneOf(EDays.class);

  private ECategory category;

  private EDateIndicator dateIndicator;

  private String location;

  private String baseLocationSuffix;

  private String associationLocationSuffix;

  private String diagramType;

  private EAssociationType associationType;

  private EStpIndicator stpIndicator;

  public AssociationElement() {
    super(CifElement.Type.ASSOCIATION);
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

  public String getAssociatedTrainUid() {
    return associatedTrainUid;
  }

  public void setAssociatedTrainUid(String associatedTrainUid) {
    this.associatedTrainUid = associatedTrainUid;
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

  public EnumSet<EDays> getDays() {
    return days;
  }

  public void setDays(EnumSet<EDays> days) {
    this.days = days;
  }

  public ECategory getCategory() {
    return category;
  }

  public void setCategory(ECategory category) {
    this.category = category;
  }

  public EDateIndicator getDateIndicator() {
    return dateIndicator;
  }

  public void setDateIndicator(EDateIndicator dateIndicator) {
    this.dateIndicator = dateIndicator;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getBaseLocationSuffix() {
    return baseLocationSuffix;
  }

  public void setBaseLocationSuffix(String baseLocationSuffix) {
    this.baseLocationSuffix = baseLocationSuffix;
  }

  public String getAssociationLocationSuffix() {
    return associationLocationSuffix;
  }

  public void setAssociationLocationSuffix(String associationLocationSuffix) {
    this.associationLocationSuffix = associationLocationSuffix;
  }

  public String getDiagramType() {
    return diagramType;
  }

  public void setDiagramType(String diagramType) {
    this.diagramType = diagramType;
  }

  public EAssociationType getAssociationType() {
    return associationType;
  }

  public void setAssociationType(EAssociationType associationType) {
    this.associationType = associationType;
  }

  public EStpIndicator getStpIndicator() {
    return stpIndicator;
  }

  public void setStpIndicator(EStpIndicator stpIndicator) {
    this.stpIndicator = stpIndicator;
  }
}
