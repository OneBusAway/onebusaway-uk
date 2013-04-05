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

public class BerthStepIdentifier extends TrackIdentifier {
  private static final long serialVersionUID = 1L;

  private final String fromBerthId;

  private final String toBerthId;

  public BerthStepIdentifier(String fromBerthId, String toBerthId) {
    this.fromBerthId = fromBerthId;
    this.toBerthId = toBerthId;
  }

  public BerthStepIdentifier(String areaId, String fromBerthId, String toBerthId) {
    this(areaId + "_" + fromBerthId, areaId + "_" + toBerthId);
  }

  public String getFromBerthId() {
    return fromBerthId;
  }

  public String getToBerthId() {
    return toBerthId;
  }

  /**
   * Berth step identifiers typically take the form AREA_FROM-AREA_TO. We
   * consider two berth step identifiers to be directly linked if AREA_TO of the
   * first step equals AREA_FROM of the second step, indicating that the steps
   * are linked in the physical rail network.
   * 
   * Step identifiers can be linked in other ways. When two step identifiers
   * transition between berth areas, we've noticed the following convention:
   * 
   * TB_L123-TB_L456 => LA_T123-LA_T456
   * 
   * @param next
   * @return true if the step identifiers are directly linked in the physical
   *         rail network.
   */
  public boolean isDirectlyLinked(BerthStepIdentifier next) {
    if (toBerthId.equals(next.getFromBerthId())) {
      return true;
    }
    Id fromA = parseId(fromBerthId);
    Id fromB = parseId(toBerthId);
    Id toA = parseId(next.fromBerthId);
    Id toB = parseId(next.toBerthId);
    if (fromA.areaId.equals(toA.areaId)) {
      return false;
    }
    String fromASubId = fromA.berthId.substring(1);
    String fromBSubId = fromB.berthId.substring(1);
    String toASubId = toA.berthId.substring(1);
    String toBSubId = toB.berthId.substring(1);
    if (fromASubId.equals(toASubId) && fromBSubId.equals(toBSubId)) {
      return true;
    }

    return false;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((fromBerthId == null) ? 0 : fromBerthId.hashCode());
    result = prime * result + ((toBerthId == null) ? 0 : toBerthId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    BerthStepIdentifier other = (BerthStepIdentifier) obj;
    if (fromBerthId == null) {
      if (other.fromBerthId != null)
        return false;
    } else if (!fromBerthId.equals(other.fromBerthId))
      return false;
    if (toBerthId == null) {
      if (other.toBerthId != null)
        return false;
    } else if (!toBerthId.equals(other.toBerthId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return fromBerthId + "-" + toBerthId;
  }

  private static Id parseId(String id) {
    int index = id.indexOf('_');
    if (index == -1) {
      throw new IllegalStateException("invalid id: " + id);
    }
    Id obj = new Id();
    obj.areaId = id.substring(0, index);
    obj.berthId = id.substring(index + 1);
    if (obj.areaId.length() != 2) {
      throw new IllegalStateException("invalid area id: " + id);
    }
    if (obj.berthId.length() != 4) {
      throw new IllegalStateException("invalid berth id: " + id);
    }
    return obj;
  }

  private static class Id {
    public String areaId;
    public String berthId;
  }
}