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

  private Map<String, ETrainMovementMessageType> _prevState = new HashMap<String, ETrainMovementMessageType>();

  private SortedMap<String, Long> _stateTranistionCounts = new TreeMap<String, Long>();

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
  }

  private void count(String key, long count, Map<String, String> status) {
    status.put("network_rail_gtfs_realtime.statistics." + key,
        Long.toString(count));
  }
}
