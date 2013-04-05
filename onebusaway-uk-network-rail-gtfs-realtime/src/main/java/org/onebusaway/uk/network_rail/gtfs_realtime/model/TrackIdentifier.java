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

public abstract class TrackIdentifier implements Serializable {

  private static final long serialVersionUID = 1L;

  public static TrackIdentifier getStanoxIdentifier(int stanox) {
    return new StanoxIdentifier(stanox);
  }

  public static BerthStepIdentifier getBerthIdentifier(String combinedBerthId) {
    String[] tokens = combinedBerthId.split("-");
    return getBerthIdentifier(tokens[0], tokens[1]);
  }
  
  public static BerthStepIdentifier getBerthIdentifier(String fromBerthId,
      String toBerthId) {
    return new BerthStepIdentifier(fromBerthId, toBerthId);
  }
}
