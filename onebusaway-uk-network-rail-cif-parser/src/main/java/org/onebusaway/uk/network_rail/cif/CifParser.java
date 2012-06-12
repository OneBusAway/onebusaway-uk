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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.TimeZone;

import org.onebusaway.uk.network_rail.cif.AssociationElement.EAssociationType;
import org.onebusaway.uk.network_rail.cif.AssociationElement.ECategory;
import org.onebusaway.uk.network_rail.cif.AssociationElement.EDateIndicator;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement.EBankHolidayRunning;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement.EStatus;
import org.onebusaway.uk.network_rail.cif.CifElement.Type;
import org.onebusaway.uk.parser.AbstractParser;
import org.onebusaway.uk.parser.ContentHandler;
import org.onebusaway.uk.parser.Element;
import org.onebusaway.uk.parser.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CifParser extends AbstractParser<CifElement.Type> {

  private static Logger _log = LoggerFactory.getLogger(CifParser.class);

  private final DateFormat _yearMonthDayDateFormat = new SimpleDateFormat(
      "yyMMdd");

  private BasicScheduleElement _currentSchedule = null;

  public CifParser() {
    _typesByKey.put("HD", CifElement.Type.HEADER);
    _typesByKey.put("TI", CifElement.Type.TIPLOC_INSERT);
    _typesByKey.put("AA", CifElement.Type.ASSOCIATION);
    _typesByKey.put("BS", CifElement.Type.BASIC_SCHEDULE);
    _typesByKey.put("BX", CifElement.Type.BASIC_SCHEDULE_EXTRA_DETAILS);
    _typesByKey.put("LO", CifElement.Type.ORIGIN_LOCATION);
    _typesByKey.put("LI", CifElement.Type.INTERMEDIATE_LOCATION);
    _typesByKey.put("LT", CifElement.Type.TERMINATING_LOCATION);
    _typesByKey.put("CR", CifElement.Type.CHANGE_EN_ROUTE);
    _typesByKey.put("ZZ", CifElement.Type.UNKNOWN);

    TimeZone tz = TimeZone.getTimeZone("Europe/London");
    _yearMonthDayDateFormat.setTimeZone(tz);
  }

  @Override
  protected boolean handleRecordType(Type type, ContentHandler handler) {
    switch (type) {
      case HEADER:
        parseHeader(handler);
        break;
      case TIPLOC_INSERT:
        parseTiplocInsert(handler);
        break;
      case ASSOCIATION:
        parseAssociation(handler);
        break;
      case BASIC_SCHEDULE:
        parseBasicSchedule(handler);
        break;
      case BASIC_SCHEDULE_EXTRA_DETAILS:
        parseBasicScheduleExtraDetails(handler);
        break;
      case ORIGIN_LOCATION:
        parseOriginLocation(handler);
        break;
      case INTERMEDIATE_LOCATION:
        parseIntermediateLocation(handler);
        break;
      case TERMINATING_LOCATION:
        parseTerminatingLocation(handler);
        break;
      case CHANGE_EN_ROUTE:
        parseChangeEnRoute(handler);
        break;
      case UNKNOWN:
        break;
      default:
        throw new ParserException("unhandled record type: " + type);
    }
    return true;
  }

  @Override
  protected void closeOpenElementIfNeeded(Element element,
      ContentHandler handler) {
    if ((element == null || !(element instanceof ScheduleChildElement))
        && _currentSchedule != null) {
      handler.endElement(_currentSchedule);
      _currentSchedule = null;
    }
  }

  private void parseHeader(ContentHandler handler) {
    HeaderElement element = element(new HeaderElement());

    SimpleDateFormat extractTimeformat = new SimpleDateFormat("ddMMyyHHmm");
    extractTimeformat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
    SimpleDateFormat extractRangeFormat = new SimpleDateFormat("ddMMyy");
    extractRangeFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));

    element.setFileMainframeIdentity(pop(20));
    element.setExtractTime(parseDate(extractTimeformat, pop(10)));
    element.setCurrentFileRef(pop(7));
    element.setLastFileRef(pop(7));
    element.setUpdateIndicator(pop(1));
    element.setVersion(pop(1));
    element.setExtractStartDate(parseDate(extractRangeFormat, pop(6)));
    element.setExtractEndDate(parseDate(extractRangeFormat, pop(6)));

    fireElement(element, handler);
  }

  private void parseTiplocInsert(ContentHandler handler) {
    TiplocInsertElement element = element(new TiplocInsertElement());

    element.setTiploc(pop(7));
    element.setTiplocCapitalization(integer(pop(2)));
    element.setNalco(integer(pop(6)));
    element.setNlcCheckCharacter(pop(1));
    element.setTpsDescription(pop(26));
    element.setStanox(integer(pop(5)));
    element.setPostOfficeLocationCode(integer(pop(4)));
    element.setCrsCode(pop(3));
    element.setCapriDescription(pop(16));

    fireElement(element, handler);
  }

  private void parseAssociation(ContentHandler handler) {
    AssociationElement element = element(new AssociationElement());
    element.setTransactionType(popTransactionType());
    element.setTrainUid(pop(6));
    element.setAssociatedTrainUid(pop(6));
    element.setStartDate(popYearMonthDay());
    element.setEndDate(popYearMonthDay());
    element.setDays(popDays());
    element.setCategory(popAssociationCategory());
    element.setDateIndicator(popDateIndicator());
    element.setLocation(pop(7));
    element.setBaseLocationSuffix(pop(1));
    element.setAssociationLocationSuffix(pop(1));
    element.setDiagramType(pop(1));
    element.setAssociationType(popAssociationType());
    pop(31);
    element.setStpIndicator(popStpIndicator());
    fireElement(element, handler);
  }

  private ETransactionType popTransactionType() {
    String type = pop(1);
    switch (type.charAt(0)) {
      case 'N':
        return ETransactionType.NEW;
      case 'D':
        return ETransactionType.DELETE;
      case 'R':
        return ETransactionType.REVISE;
      default:
        throw new ParserException("unknown transaction type: " + type);
    }
  }

  private EnumSet<EDays> popDays() {
    String value = pop(7);
    EnumSet<EDays> days = EnumSet.noneOf(EDays.class);
    EDays[] inOrder = EDays.values();
    for (int i = 0; i < 7; ++i) {
      if (value.charAt(i) == '1') {
        days.add(inOrder[i]);
      }
    }
    return days;
  }

  private EStpIndicator popStpIndicator() {
    String value = pop(1);
    switch (value.charAt(0)) {
      case 'C':
        return EStpIndicator.CANCELATION;
      case 'N':
        return EStpIndicator.NEW;
      case 'P':
        return EStpIndicator.PERMANENT;
      case 'O':
        return EStpIndicator.OVERLAY;
      default:
        throw new ParserException("unknown stp indicator=" + value);
    }
  }

  private EDateIndicator popDateIndicator() {
    String indicator = pop(1);
    if (indicator.isEmpty()) {
      return null;
    }
    switch (indicator.charAt(0)) {
      case 'S':
        return EDateIndicator.STANDARD;
      case 'N':
        return EDateIndicator.OVER_NEXT_MIDNIGHT;
      case 'P':
        return EDateIndicator.OVER_PREVIOUS_MIDNIGHT;
      case 'U':
        _log.warn("unknown date indicator=" + indicator);
        return null;
      default:
        throw new ParserException("unknown association date indicator="
            + indicator);
    }
  }

  private ECategory popAssociationCategory() {
    String value = pop(2);
    if (value.isEmpty()) {
      return null;
    }
    if (value.equals("JJ")) {
      return ECategory.JOIN;
    } else if (value.equals("VV")) {
      return ECategory.DIVIDE;
    } else if (value.equals("NP")) {
      return ECategory.NEXT;
    }
    throw new ParserException("unknown association category=" + value);
  }

  private EAssociationType popAssociationType() {
    String value = pop(1);
    if (value.isEmpty()) {
      return null;
    }
    switch (value.charAt(0)) {
      case 'P':
        return EAssociationType.PASSENGER;
      case 'O':
        return EAssociationType.OPERATING;
      default:
        throw new ParserException("unknown association type=" + value);
    }
  }

  private void parseBasicSchedule(ContentHandler handler) {
    BasicScheduleElement element = element(new BasicScheduleElement());
    element.setTransactionType(popTransactionType());
    element.setTrainUid(pop(6));
    element.setRunsFrom(popYearMonthDay());
    element.setRunsTo(popYearMonthDay());
    element.setDays(popDays());
    element.setBankHolidayDates(popBankHolidayDates());
    element.setStatus(popTrainStatus());
    pop(2); // Train Category
    element.setIdentity(pop(4));
    element.setHeadcode(pop(4));
    pop(1); // Course Indicator
    element.setServiceCode(pop(8));
    pop(1); // Portion Id
    pop(3); // Power Type
    pop(4); // Timing Load
    pop(3); // Speed
    pop(6); // Operating Characteristics
    pop(1); // Train Class
    pop(1); // Sleepers
    pop(1); // Reservations
    pop(1); // Connection Indicator
    pop(4); // Catering Code
    pop(4); // Service Branding
    pop(1); // Spare
    element.setStpIndicator(popStpIndicator());

    closeOpenElementIfNeeded(null, handler);
    _currentSchedule = element;
    handler.startElement(element);
  }

  private EBankHolidayRunning popBankHolidayDates() {
    String value = pop(1);
    if (value.isEmpty()) {
      return null;
    }
    switch (value.charAt(0)) {
      case 'X':
        return EBankHolidayRunning.BANK_HOLIDAY_DATES;
      case 'E':
        return EBankHolidayRunning.EDINBURGH_HOLIDAY_DATES;
      case 'G':
        return EBankHolidayRunning.GLASGOW_HOLIDAY_DATES;
      default:
        throw new ParserException("unknown bank holiday dates=" + value);
    }
  }

  private EStatus popTrainStatus() {
    String value = pop(1);
    if (value.isEmpty()) {
      return null;
    }
    switch (value.charAt(0)) {
      case 'B':
        return EStatus.BUS;
      case 'F':
        return EStatus.FREIGHT;
      case 'P':
        return EStatus.PASSENGER_AND_PARCELS;
      case 'S':
        return EStatus.SHIP;
      case 'T':
        return EStatus.TRIP;
      case '1':
        return EStatus.STP_PASSENGER_AND_PARCELS;
      case '2':
        return EStatus.STP_FREIGHT;
      case '3':
        return EStatus.STP_TRIP;
      case '4':
        return EStatus.STP_SHIP;
      case '5':
        return EStatus.STP_BUS;
      default:
        throw new ParserException("unknown train status=" + value);
    }
  }

  private void parseBasicScheduleExtraDetails(ContentHandler handler) {
    BasicScheduleExtraDetailsElement element = element(new BasicScheduleExtraDetailsElement());

    fireElement(element, handler);
  }

  private void parseOriginLocation(ContentHandler handler) {
    OriginLocationElement element = element(new OriginLocationElement());
    element.setTiploc(pop(7));
    element.setTiplocSuffix(pop(1));
    element.setScheduledDepartureTime(time(pop(5)));
    element.setPublicDepartureTime(time(pop(4)));
    element.setPlatform(pop(3));
    element.setLine(pop(3));
    element.setEngineeringAllowance(duration(pop(2)));
    element.setPathingAllowance(duration(pop(2)));
    element.setActivity(pop(12));
    element.setPerformanceAllowance(duration(pop(2)));
    pushTimepointElement(element, handler);
  }

  private void parseIntermediateLocation(ContentHandler handler) {
    IntermediateLocationElement element = element(new IntermediateLocationElement());
    element.setTiploc(pop(7));
    element.setTiplocSuffix(pop(1));
    element.setScheduledArrivalTime(time(pop(5)));
    element.setScheduledDepartureTime(time(pop(5)));
    element.setScheduledPassTime(time(pop(5)));
    element.setPublicArrivalTime(time(pop(4)));
    element.setPublicDepartureTime(time(pop(4)));
    element.setPlatform(pop(3));
    element.setLine(pop(3));
    element.setPath(pop(3));
    element.setActivity(pop(12));
    element.setEngineeringAllowance(duration(pop(2)));
    element.setPathingAllowance(duration(pop(2)));
    element.setPerformanceAllowance(duration(pop(2)));
    pushTimepointElement(element, handler);
  }

  private void parseTerminatingLocation(ContentHandler handler) {
    TerminatingLocationElement element = element(new TerminatingLocationElement());
    element.setTiploc(pop(7));
    element.setTiplocSuffix(pop(1));
    element.setScheduledArrivalTime(time(pop(5)));
    element.setPublicArrivalTime(time(pop(4)));
    element.setPlatform(pop(3));
    element.setPath(pop(3));
    element.setActivity(pop(12));
    pushTimepointElement(element, handler);
  }

  private void parseChangeEnRoute(ContentHandler handler) {
    ChangeEnRouteElement element = element(new ChangeEnRouteElement());
    fireElement(element, handler);
  }

  private int time(String pop) {
    if (pop.isEmpty()) {
      return 0;
    }
    int sec = pop.endsWith("H") ? 30 : 0;
    int hour = integer(pop.substring(0, 2));
    int min = integer(pop.substring(2, 4));
    return ((hour * 60) + min) * 60 + sec;
  }

  private Date popYearMonthDay() {
    String dateValue = pop(6);
    if (dateValue.equals("999999")) {
      return null;
    }
    return parseDate(_yearMonthDayDateFormat, dateValue);
  }

  private int duration(String value) {
    String rawValue = value;
    int duration = 0;
    if (value.endsWith("H")) {
      value = value.substring(0, value.length() - 1);
      duration += 30;
    }
    if (!value.isEmpty()) {
      try {
        duration += Integer.parseInt(value) * 60;
      } catch (NumberFormatException ex) {
        throw new ParserException("error parsing duration value=" + rawValue);
      }
    }
    return duration;
  }

  private void pushTimepointElement(TimepointElement element,
      ContentHandler handler) {
    if (_currentSchedule == null)
      throw new ParserException("location without basic schedule at "
          + describeLineLocation());
    element.setSchedule(_currentSchedule);
    _currentSchedule.getTimepoints().add(element);
    fireElement(element, handler);
  }

}
