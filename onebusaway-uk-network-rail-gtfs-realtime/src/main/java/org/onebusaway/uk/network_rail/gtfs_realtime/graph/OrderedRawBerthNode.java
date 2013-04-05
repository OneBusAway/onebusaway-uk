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