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

public class TrainMovementHeader {
  private String msgType;

  private String sourceDevId;

  private String userId;

  private String originalDataSource;

  private String msgQueueTimestamp;

  private String sourceSystemId;

  public String getMsgType() {
    return msgType;
  }

  public void setMsgType(String msgType) {
    this.msgType = msgType;
  }

  public String getSourceDevId() {
    return sourceDevId;
  }

  public void setSourceDevId(String sourceDevId) {
    this.sourceDevId = sourceDevId;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getOriginalDataSource() {
    return originalDataSource;
  }

  public void setOriginalDataSource(String originalDataSource) {
    this.originalDataSource = originalDataSource;
  }

  public String getMsgQueueTimestamp() {
    return msgQueueTimestamp;
  }

  public void setMsgQueueTimestamp(String msgQueueTimestamp) {
    this.msgQueueTimestamp = msgQueueTimestamp;
  }

  public String getSourceSystemId() {
    return sourceSystemId;
  }

  public void setSourceSystemId(String sourceSystemId) {
    this.sourceSystemId = sourceSystemId;
  }
}
