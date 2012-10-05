package org.onebusaway.uk.network_rail.gtfs_realtime.model;

import java.text.DateFormat;
import java.util.Date;

public class Event implements Comparable<Event> {

  private static final DateFormat _format = DateFormat.getDateTimeInstance(
      DateFormat.SHORT, DateFormat.SHORT);

  private final long timestamp;

  private final String source;

  public Event(long timestamp, String source) {
    this.timestamp = timestamp;
    this.source = source;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public TrackIdentifier getTrackIdentifier() {
    return null;
  }

  public String toLogString() {
    return _format.format(new Date(timestamp)) + " " + source;
  }

  @Override
  public int compareTo(Event o) {
    return this.timestamp == o.timestamp ? 0 : (this.timestamp < o.timestamp
        ? -1 : 1);
  }

  @Override
  public String toString() {
    return _format.format(new Date(timestamp));
  }

}