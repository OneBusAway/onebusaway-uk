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

public enum ETrainClass {

  EXPRESS_PASSENGER_TRAIN(1),

  PASSENGER_TRAIN(2),

  FREIGHT_TRAIN(3),

  FREIGHT_TRAIN_75(4),

  EMPTY_COACHING_STOCK(5),

  FREIGHT_TRAIN_60(6),

  FREIGHT_TRAIN_45(7),

  FREIGHT_TRAIN_35(8),

  EUROSTAR(9),

  OTHER(0);

  private int code;

  ETrainClass(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public boolean isPassengerClass() {
    return this == EXPRESS_PASSENGER_TRAIN || this == PASSENGER_TRAIN
        || this == EUROSTAR;
  }

  public static ETrainClass getTrainClassForCode(int code) {
    switch (code) {
      case 1:
        return EXPRESS_PASSENGER_TRAIN;
      case 2:
        return PASSENGER_TRAIN;
      case 3:
        return FREIGHT_TRAIN;
      case 4:
        return FREIGHT_TRAIN_75;
      case 5:
        return EMPTY_COACHING_STOCK;
      case 6:
        return FREIGHT_TRAIN_60;
      case 7:
        return FREIGHT_TRAIN_45;
      case 8:
        return FREIGHT_TRAIN_35;
      case 9:
        return EUROSTAR;
      case 0:
      default:
        return OTHER;
    }
  }
}
