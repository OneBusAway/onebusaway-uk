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

import java.io.Serializable;

public final class TrainState implements Serializable {

  private static final long serialVersionUID = 2L;

  private String trainId;

  private String trainUid;
  
  private long serviceDate;

  private long lastUpdateTimestamp;

  public String getTrainId() {
    return trainId;
  }

  public void setTrainId(String trainId) {
    this.trainId = trainId;
  }

  public String getTrainUid() {
    return trainUid;
  }

  public void setTrainUid(String trainUid) {
    this.trainUid = trainUid;
  }

  public long getServiceDate() {
    return serviceDate;
  }

  public void setServiceDate(long serviceDate) {
    this.serviceDate = serviceDate;
  }

  public long getLastUpdateTimestamp() {
    return lastUpdateTimestamp;
  }

  public void setLastUpdateTimestamp(long lastLocalUpdateTimestamp) {
    this.lastUpdateTimestamp = lastLocalUpdateTimestamp;
  }
}
