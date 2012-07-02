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
package org.onebusaway.uk.network_rail.gtfs_realtime.model;

public class BerthMessage {

  private String areaId;

  /**
   * This appears to be the train reporting number:
   * 
   * http://en.wikipedia.org/wiki/Train_reporting_number
   * 
   * A four-character code with the following semantics:
   * 
   * - a single digit indicating the train class / type
   * 
   * - a letter indicating the destination area
   * 
   * - a two-digit number identifying the individual train
   */
  private String descr;

  private String msgType;

  private String time;

  public String getAreaId() {
    return areaId;
  }

  public void setAreaId(String areaId) {
    this.areaId = areaId;
  }

  public String getDescr() {
    return descr;
  }

  public void setDescr(String descr) {
    this.descr = descr;
  }

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public long getTimeAsLong() {
    return Long.parseLong(this.time);
  }

}
