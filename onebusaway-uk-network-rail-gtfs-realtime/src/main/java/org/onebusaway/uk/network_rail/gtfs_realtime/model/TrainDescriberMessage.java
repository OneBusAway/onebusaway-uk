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

import com.google.gson.annotations.SerializedName;

public class TrainDescriberMessage {
  @SerializedName("CA_MSG")
  private BerthStepMessage step;

  @SerializedName("CB_MSG")
  private BerthCancelMessage cancel;

  @SerializedName("CC_MSG")
  private BerthInterposeMessage interpose;

  @SerializedName("CT_MSG")
  private BerthHeartbeatMessage heartbeat;

  public BerthStepMessage getStep() {
    return step;
  }

  public void setStep(BerthStepMessage step) {
    this.step = step;
  }

  public BerthCancelMessage getCancel() {
    return cancel;
  }

  public void setCancel(BerthCancelMessage cancel) {
    this.cancel = cancel;
  }

  public BerthInterposeMessage getInterpose() {
    return interpose;
  }

  public void setInterpose(BerthInterposeMessage interpose) {
    this.interpose = interpose;
  }

  public BerthHeartbeatMessage getHeartbeat() {
    return heartbeat;
  }

  public void setHeartbeat(BerthHeartbeatMessage heartbeat) {
    this.heartbeat = heartbeat;
  }
}
