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

/**
 * @author bdferris
 * 
 */
public class HeaderElement extends CifElement {

  private String fileMainframeIdentity;

  private Date extractTime;

  private String currentFileRef;

  private String lastFileRef;

  private String updateIndicator;

  private String version;

  private Date extractStartDate;

  private Date extractEndDate;

  public HeaderElement() {
    super(CifElement.Type.HEADER);
  }

  public String getFileMainframeIdentity() {
    return fileMainframeIdentity;
  }

  public void setFileMainframeIdentity(String fileMainframeIdentity) {
    this.fileMainframeIdentity = fileMainframeIdentity;
  }

  public Date getExtractTime() {
    return extractTime;
  }

  public void setExtractTime(Date extractTime) {
    this.extractTime = extractTime;
  }

  public String getCurrentFileRef() {
    return currentFileRef;
  }

  public void setCurrentFileRef(String currentFileRef) {
    this.currentFileRef = currentFileRef;
  }

  public String getLastFileRef() {
    return lastFileRef;
  }

  public void setLastFileRef(String lastFileRef) {
    this.lastFileRef = lastFileRef;
  }

  public String getUpdateIndicator() {
    return updateIndicator;
  }

  public void setUpdateIndicator(String updateIndicator) {
    this.updateIndicator = updateIndicator;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public Date getExtractStartDate() {
    return extractStartDate;
  }

  public void setExtractStartDate(Date extractStartDate) {
    this.extractStartDate = extractStartDate;
  }

  public Date getExtractEndDate() {
    return extractEndDate;
  }

  public void setExtractEndDate(Date extractEndDate) {
    this.extractEndDate = extractEndDate;
  }
}
