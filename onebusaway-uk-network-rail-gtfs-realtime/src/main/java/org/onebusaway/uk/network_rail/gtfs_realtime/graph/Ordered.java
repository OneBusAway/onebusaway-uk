package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

public class Ordered<T> implements Comparable<Ordered<T>> {
  private final T value;
  private final int count;

  public Ordered(T value, int count) {
    this.value = value;
    this.count = count;
  }

  public T getValue() {
    return value;
  }

  public int getCount() {
    return count;
  }

  @Override
  public int compareTo(Ordered<T> o) {
    return this.count - o.count;
  }

  @Override
  public String toString() {
    return count + " " + value;
  }
}
