package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Statistics {

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

    if (_messageCount % 1000 == 0) {
      print();
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

  private void print() {
    System.out.println("========================================");
    System.out.println("                  messages=" + _messageCount);
    System.out.println("           unknownTrainUid=" + _unknownTrainUidCount);
    System.out.println("            unknownTrainId=" + _unknownTrainIdCount);
    System.out.println("   unknownCancelledTrainId="
        + _unknownCancelledTrainIdCount);
    System.out.println("  unknownReinstatedTrainId="
        + _unknownReinstatedTrainIdCount);
    System.out.println("            emptyLocStanox=" + _emptyLocStanoxCount);
    System.out.println("             unknownStanox=" + _unknownStanoxCount);
    System.out.println("      trainActivationCount=" + _trainActivationCount);
    System.out.println("    trainCancellationCount=" + _trainCancellationCount);
    System.out.println("        trainMovementCount=" + _trainMovementCount);
    System.out.println("    unidentifiedTrainCount=" + _unidentifiedTrainCount);
    System.out.println("   trainReinstatementCount=" + _trainReinstatementCount);
    System.out.println("  trainChangeOfOriginCount="
        + _trainChangeOfOriginCount);
    System.out.println("trainChangeOfIdentityCount="
        + _trainChangeOfIdentityCount);
    System.out.println("trainChangeOfLocationCount="
        + _trainChangeOfLocationCount);

    for (Map.Entry<String, Long> entry : _stateTranistionCounts.entrySet()) {
      System.out.println(entry.getKey() + " = " + entry.getValue());
    }
  }
}
