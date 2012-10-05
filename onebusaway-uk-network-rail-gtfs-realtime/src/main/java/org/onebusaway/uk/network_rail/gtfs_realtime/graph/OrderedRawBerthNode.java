package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

class OrderedRawBerthNode implements Comparable<OrderedRawBerthNode> {

  private final RawBerthNode node;
  private final RawBerthNode parent;
  private final double distance;
  private boolean open;

  public OrderedRawBerthNode(RawBerthNode node, RawBerthNode parent,
      double distance) {
    this(node, parent, distance, true);
  }

  public OrderedRawBerthNode(RawBerthNode node, RawBerthNode parent,
      double distance, boolean open) {
    this.node = node;
    this.parent = parent;
    this.distance = distance;
    this.open = open;
  }

  public RawBerthNode getNode() {
    return node;
  }

  public RawBerthNode getParent() {
    return parent;
  }

  public double getDistance() {
    return distance;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  @Override
  public int compareTo(OrderedRawBerthNode o) {
    return Double.compare(this.distance, o.distance);
  }

  @Override
  public String toString() {
    return node + " " + distance;
  }
}