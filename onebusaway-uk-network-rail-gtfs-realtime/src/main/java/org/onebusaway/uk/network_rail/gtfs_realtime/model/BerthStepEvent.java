package org.onebusaway.uk.network_rail.gtfs_realtime.model;


public class BerthStepEvent extends Event {

  private final BerthStepIdentifier id;

  public BerthStepEvent(long timestamp, String source, BerthStepIdentifier id) {
    super(timestamp, source);
    this.id = id;
  }

  @Override
  public TrackIdentifier getTrackIdentifier() {
    return id;
  }

  @Override
  public String toLogString() {
    return super.toLogString() + " td " + id;
  }

  @Override
  public String toString() {
    return toLogString();
  }
}