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
  private CAMessage caMessage;

  @SerializedName("CB_MSG")
  private CBMessage cbMessage;

  @SerializedName("CC_MSG")
  private CCMessage ccMessage;

  @SerializedName("CT_MSG")
  private CTMessage ctMessage;

  public CAMessage getCaMessage() {
    return caMessage;
  }

  public void setCaMessage(CAMessage caMessage) {
    this.caMessage = caMessage;
  }

  public CBMessage getCbMessage() {
    return cbMessage;
  }

  public void setCbMessage(CBMessage cbMessage) {
    this.cbMessage = cbMessage;
  }

  public CCMessage getCcMessage() {
    return ccMessage;
  }

  public void setCcMessage(CCMessage ccMessage) {
    this.ccMessage = ccMessage;
  }

  public CTMessage getCtMessage() {
    return ctMessage;
  }

  public void setCtMessage(CTMessage ctMessage) {
    this.ctMessage = ctMessage;
  }
}
