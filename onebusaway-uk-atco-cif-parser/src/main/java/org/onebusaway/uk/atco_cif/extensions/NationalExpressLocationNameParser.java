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
package org.onebusaway.uk.atco_cif.extensions;

import org.onebusaway.uk.parser.ContentHandler;
import org.onebusaway.uk.parser.ExtensionParser;
import org.onebusaway.uk.parser.ParserInstance;

public class NationalExpressLocationNameParser implements ExtensionParser {

  @Override
  public boolean handleExtensionRecordType(ParserInstance parser,
      ContentHandler handler) {
    NationalExpressLocationNameElement element = parser.element(new NationalExpressLocationNameElement());
    parser.pop(1);
    element.setLocationId(parser.pop(12));
    element.setShortName(parser.pop(50));
    element.setFullName(parser.pop(160));
    parser.fireElement(element, handler);
    return true;
  }
}
