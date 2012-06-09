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
package org.onebusaway.uk.network_rail.cif;

import org.onebusaway.uk.parser.Element;

public class CifElement extends Element {

  public enum Type {

    HEADER,

    ASSOCIATION,

    BASIC_SCHEDULE,

    BASIC_SCHEDULE_EXTRA_DETAILS,

    ORIGIN_LOCATION,

    INTERMEDIATE_LOCATION,

    TERMINATING_LOCATION,

    CHANGE_EN_ROUTE,

    TIPLOC_INSERT,

    TIPLOC_AMMEND,

    TIPLOC_DELETE,

    UNKNOWN
  }

  private final Type type;

  public CifElement(Type type) {
    this.type = type;
  }

  public Type getType() {
    return type;
  }
}
