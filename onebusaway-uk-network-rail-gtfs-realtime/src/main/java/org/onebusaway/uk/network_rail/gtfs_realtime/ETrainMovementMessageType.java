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
package org.onebusaway.uk.network_rail.gtfs_realtime;

public enum ETrainMovementMessageType {
  ACTIVATION, CANCELLATION, MOVEMENT, UNIDENTIFIED_TRAIN, REINSTATEMENT, CHANGE_OF_ORIGIN, CHANGE_OF_IDENTITY, CHANGE_OF_LOCATION;

  public static ETrainMovementMessageType getTypeForCode(int code) {
    switch (code) {
      case 1:
        return ETrainMovementMessageType.ACTIVATION;
      case 2:
        return ETrainMovementMessageType.CANCELLATION;
      case 3:
        return ETrainMovementMessageType.MOVEMENT;
      case 4:
        return ETrainMovementMessageType.UNIDENTIFIED_TRAIN;
      case 5:
        return ETrainMovementMessageType.REINSTATEMENT;
      case 6:
        return ETrainMovementMessageType.CHANGE_OF_ORIGIN;
      case 7:
        return ETrainMovementMessageType.CHANGE_OF_IDENTITY;
      case 8:
        return ETrainMovementMessageType.CHANGE_OF_LOCATION;
      default:
        return null;
    }
  }
}
