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