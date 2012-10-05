package org.onebusaway.uk.network_rail.gtfs_realtime.instance;

public interface TrainTrackingListener {
  public void handlePrunedTrainReportingNumberInstance(
      TrainReportingNumberInstance instance);

  public void handlePrunedTrainInstance(TrainInstance trainInstance);
}
