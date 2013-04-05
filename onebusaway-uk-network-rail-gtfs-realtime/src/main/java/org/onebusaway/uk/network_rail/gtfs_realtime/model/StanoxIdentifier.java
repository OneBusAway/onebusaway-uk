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

public class StanoxIdentifier extends TrackIdentifier {

  private static final long serialVersionUID = 1L;

  private final int stanox;

  public StanoxIdentifier(int stanox) {
    this.stanox = stanox;
  }

  public int getStanox() {
    return stanox;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + stanox;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StanoxIdentifier other = (StanoxIdentifier) obj;
    if (stanox != other.stanox)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return Integer.toString(stanox);
  }
}