/**
 * Copyright (C) 2012 Google, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.inject.Singleton;

import org.onebusaway.status_exporter.StatusProviderService;

@Singleton
public class StatisticsService implements StatusProviderService {

  private long _messageCount;

  private long _unknownTrainUidCount;

  private long _unknownTrainIdCount;

  private long _unknownCancelledTrainIdCount;

  private long _unknownReinstatedTrainIdCount;

  private long _emptyLocStanoxCount;

  private long _unknownStanoxCount;

  private long _trainActivationCount;

  private long _trainCancellationCount;

  private long _trainMovementCount;

  private long _unidentifiedTrainCount;

  private long _trainReinstatementCount;

  private long _trainChangeOfOriginCount;

  private long _trainChangeOfIdentityCount;

  private long _trainChangeOfLocationCount;

  private long _matchedBethStepCount;

  private long _unmatchedBethStepCount;

  private long _platformChange;

  private Map<String, ETrainMovementMessageType> _prevState = new HashMap<String, ETrainMovementMessageType>();

  private SortedMap<String, Long> _stateTranistionCounts = new TreeMap<String, Long>();

  private Map<String, Long> _trainClassCounts = new HashMap<String, Long>();

  public void incrementMessage(ETrainMovementMessageType msgType, String trainId) {
    _messageCount++;
    switch (msgType) {
      case ACTIVATION:
        _trainActivationCount++;
        break;
      case CANCELLATION:
        _trainCancellationCount++;
        break;
      case MOVEMENT:
        _trainMovementCount++;
        break;
      case UNIDENTIFIED_TRAIN:
        _unidentifiedTrainCount++;
        break;
      case REINSTATEMENT:
        _trainReinstatementCount++;
        break;
      case CHANGE_OF_ORIGIN:
        _trainChangeOfOriginCount++;
        break;
      case CHANGE_OF_IDENTITY:
        _trainChangeOfIdentityCount++;
        break;
      case CHANGE_OF_LOCATION:
        _trainChangeOfLocationCount++;
        break;
    }

    if (trainId != null && !trainId.isEmpty()) {
      ETrainMovementMessageType prev = _prevState.put(trainId, msgType);
      if (prev != null) {
        String key = prev + " => " + msgType;
        Long count = _stateTranistionCounts.get(key);
        count = count == null ? 1 : count + 1;
        _stateTranistionCounts.put(key, count);
      }
    }
  }

  public void incrementBerthTrainReportingNumber(String trainReportingNumber) {
    String trainClass = trainReportingNumber.substring(0, 1);
    increment(_trainClassCounts, trainClass);
  }

  public void incrementUnknownCancelledTrainIdCount() {
    _unknownCancelledTrainIdCount++;
  }

  public void incrementUnknownTrainIdCount() {
    _unknownTrainIdCount++;
  }

  public void incrementEmptyLocStanoxCount() {
    _emptyLocStanoxCount++;
  }

  public void incrementUnknownStanoxCount() {
    _unknownStanoxCount++;
  }

  public void incrementUnknownReinstatedTrainIdCount() {
    _unknownReinstatedTrainIdCount++;
  }

  public void incrementUnknownTrainUidCount() {
    _unknownTrainUidCount++;
  }

  public void incrementMatchedBerthStep() {
    _matchedBethStepCount++;
  }

  public void incrementUnmatchedBerthStep() {
    _unmatchedBethStepCount++;
  }

  public void incrementPlatformChange() {
    _platformChange++;
  }

  /****
   * {@link StatusProviderService} Interface
   ****/

  @Override
  public void getStatus(Map<String, String> status) {
    count("messageCount", _messageCount, status);
    count("unknownTrainUidCount", _unknownTrainUidCount, status);
    count("unknownTrainIdCount", _unknownTrainIdCount, status);
    count("unknownCancelledTrainIdCount", _unknownCancelledTrainIdCount, status);
    count("unknownReinstatedTrainIdCount", _unknownReinstatedTrainIdCount,
        status);
    count("emptyLocStanox", _emptyLocStanoxCount, status);
    count("unknownStanox", _unknownStanoxCount, status);
    count("platformChange", _platformChange, status);

    count("matchedBerthStepCount", _matchedBethStepCount, status);
    count("unmatchedBerthStepCount", _unmatchedBethStepCount, status);

    count("trainActivationCount", _trainActivationCount, status);
    count("trainCancellationCount", _trainCancellationCount, status);
    count("trainMovementCount", _trainMovementCount, status);
    count("trainMovementCount", _trainMovementCount, status);
    count("unidentifiedTrainCount", _unidentifiedTrainCount, status);
    count("trainReinstatementCount", _trainReinstatementCount, status);
    count("trainChangeOfOriginCount", _trainChangeOfOriginCount, status);
    count("trainChangeOfIdentityCount", _trainChangeOfIdentityCount, status);
    count("trainChangeOfLocationCount", _trainChangeOfLocationCount, status);

    for (Map.Entry<String, Long> entry : _stateTranistionCounts.entrySet()) {
      count("stateTransitions[" + entry.getKey() + "]", entry.getValue(),
          status);
    }

    for (Map.Entry<String, Long> entry : _trainClassCounts.entrySet()) {
      count("trainClass[" + entry.getKey() + "]", entry.getValue(), status);
    }
  }

  private void count(String key, long count, Map<String, String> status) {
    status.put("network_rail_gtfs_realtime.statistics." + key,
        Long.toString(count));
  }

  private void increment(Map<String, Long> counts, String key) {
    Long count = counts.get(key);
    counts.put(key, count == null ? 1 : count + 1);
  }

}
