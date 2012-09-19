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
package org.onebusaway.uk.atco_cif.extensions.greater_manchester;

import org.onebusaway.uk.parser.ContentHandler;
import org.onebusaway.uk.parser.ExtensionParser;
import org.onebusaway.uk.parser.ParserInstance;

public class GreaterManchesterParser implements ExtensionParser {

  @Override
  public boolean handleExtensionRecordType(String typeValue,
      ParserInstance parser, ContentHandler handler) {
    if (typeValue.equals("ZA")) {
      parseLocationRecord(parser, handler);
    } else {
      throw new IllegalStateException("unknown type format=" + typeValue);
    }
    return true;
  }

  private void parseLocationRecord(ParserInstance parser, ContentHandler handler) {
    GreaterManchesterTimetableRowListElement element = parser.element(new GreaterManchesterTimetableRowListElement());
    parser.pop(1); // Transaction type
    element.setLocationReference(parser.pop(12));
    element.setFullLocation(parser.pop(48));
    element.setPublicityPointFlag(parser.pop(2));
    element.setWorkingPointFlag(parser.pop(2));
    element.setPublicityPointFlag(parser.pop(2));
    parser.fireElement(element, handler);
  }
}
