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
package org.onebusaway.uk.atoc.timetable_parser;

import org.onebusaway.uk.atoc.timetable_parser.StationElement.ECategory;
import org.onebusaway.uk.parser.AbstractParser;
import org.onebusaway.uk.parser.ContentHandler;
import org.onebusaway.uk.parser.ParserException;

public class MasterStationsNamesParser extends AbstractParser<EStationRowType> {

  public MasterStationsNamesParser() {
    setElementTypeKeySize(1);
    _typesByKey.put("A", EStationRowType.STATION);
    _typesByKey.put("L", EStationRowType.STATION_ALIAS);
    _typesByKey.put("G", EStationRowType.IGNORE);
    _typesByKey.put("V", EStationRowType.IGNORE);
    _typesByKey.put("Z", EStationRowType.TRAILER);
  }

  @Override
  protected boolean handleRecordType(EStationRowType type,
      ContentHandler handler) {
    // Skip the header
    if (isFirstLine()) {
      return true;
    }
    switch (type) {
      case STATION:
        parseStation(handler);
        break;
      case STATION_ALIAS:
        parseStationAlias(handler);
        break;
      case TRAILER:
        return false;
    }

    return true;
  }

  private void parseStation(ContentHandler handler) {

    StationElement element = element(new StationElement());
    pop(4);
    element.setName(pop(30));
    element.setCategory(popCategory());
    element.setTiploc(pop(7));
    element.setSubsidiaryAlphaCode(pop(3));
    pop(3);
    element.setAlphaCode(pop(3));
    double y = Double.parseDouble(pop(5)) * 100;
    pop(1);
    double x = Double.parseDouble(pop(5)) * 100;

    // Point2D.Double latLon = ProjectionSupport.convertToLatLon(x, y);
    // element.setLat(latLon.y);
    // element.setLon(latLon.x);

    element.setChangeTime(integer(pop(2)));
    element.setFootnote(pop(2));
    pop(11);
    element.setRegion(pop(3));

    fireElement(element, handler);
  }

  private void parseStationAlias(ContentHandler handler) {
    StationAliasElement element = element(new StationAliasElement());
    pop(4);
    element.setName(pop(30));
    pop(1);
    element.setAlias(pop(30));

    fireElement(element, handler);
  }

  private ECategory popCategory() {
    int value = integer(pop(1));
    switch (value) {
      case 0:
        return ECategory.NOT_AN_INTERCHANGE;
      case 1:
        return ECategory.SMALL_INTERCHANGE;
      case 2:
        return ECategory.MEDIUM_INTERCHANGE;
      case 3:
        return ECategory.LARGE_INTERCHANGE;
      case 9:
        return ECategory.SUBSIDIARY;
      default:
        throw new ParserException("unknown category=" + value);
    }
  }

}
