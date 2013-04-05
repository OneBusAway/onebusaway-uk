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
package org.onebusaway.uk.atco_cif;

import java.awt.geom.Point2D;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.onebusaway.uk.atco_cif.AtcoCifElement.Type;
import org.onebusaway.uk.parser.AbstractParser;
import org.onebusaway.uk.parser.ContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jhlabs.map.proj.CoordinateSystemToCoordinateSystem;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionException;
import com.jhlabs.map.proj.ProjectionFactory;

public class AtcoCifParser extends AbstractParser<AtcoCifElement.Type> {

  private static Logger _log = LoggerFactory.getLogger(AtcoCifParser.class);

  private static final String _fromProjectionSpec = "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 "
      + "+y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.060,0.1502,0.2470,0.8421,-20.4894 +datum=OSGB36  +units=m +no_defs";

  private static final Projection _fromProjection = ProjectionFactory.fromPROJ4Specification(_fromProjectionSpec.split(" "));

  private static final String _toProjectionSpec = "+proj=latlong +ellps=WGS84 +datum=WGS84 +no_defs";

  private static final Projection _toProjection = ProjectionFactory.fromPROJ4Specification(_toProjectionSpec.split(" "));

  private DateFormat _serviceDateFormat = new SimpleDateFormat("yyyyMMdd");

  private JourneyHeaderElement _currentJourney = null;

  private Date _maxServiceDate;

  private double _locationScaleFactor = 1.0;

  public AtcoCifParser() {
    _typesByKey.put("QS", AtcoCifElement.Type.JOURNEY_HEADER);
    _typesByKey.put("QE", AtcoCifElement.Type.JOURNEY_DATE_RUNNING);
    _typesByKey.put("QO", AtcoCifElement.Type.JOURNEY_ORIGIN);
    _typesByKey.put("QI", AtcoCifElement.Type.JOURNEY_INTERMEDIATE);
    _typesByKey.put("QT", AtcoCifElement.Type.JOURNEY_DESTINATION);
    _typesByKey.put("QL", AtcoCifElement.Type.LOCATION);
    _typesByKey.put("QB", AtcoCifElement.Type.ADDITIONAL_LOCATION);
    _typesByKey.put("QV", AtcoCifElement.Type.VEHICLE_TYPE);
    _typesByKey.put("QC", AtcoCifElement.Type.UNKNOWN);
    _typesByKey.put("QP", AtcoCifElement.Type.OPERATOR);
    _typesByKey.put("QQ", AtcoCifElement.Type.UNKNOWN);
    _typesByKey.put("QJ", AtcoCifElement.Type.UNKNOWN);
    _typesByKey.put("QD", AtcoCifElement.Type.ROUTE_DESCRIPTION);
    _typesByKey.put("QY", AtcoCifElement.Type.UNKNOWN);
    _typesByKey.put("QG", AtcoCifElement.Type.UNKNOWN);
    setIgnoreElementTypeWithPrefix("Z");

    Calendar c = Calendar.getInstance();
    c.add(Calendar.YEAR, 2);
    _maxServiceDate = c.getTime();

    _serviceDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/London"));
  }

  /**
   * If northing and easting values used in Additional Location (QB) records
   * have been scaled by some constant fator, it can be specified here, so that
   * the values can be normalized back to meters before projection to lat-lon
   * locations.
   * 
   * @param locationScaleFactor
   */
  public void setLocationScaleFactor(double locationScaleFactor) {
    _locationScaleFactor = locationScaleFactor;
  }

  @Override
  protected boolean parseLine(ContentHandler handler) {
    if (isFirstLine()) {
      parseHeader(handler);
      return true;
    }
    return super.parseLine(handler);
  }

  private void parseHeader(ContentHandler handler) {
    String start = pop(8);
    if (!start.equals("ATCO-CIF")) {
      StringBuilder b = new StringBuilder();
      for (byte singleByte : start.getBytes()) {
        String hb = Integer.toHexString(0xff & singleByte);
        if (hb.length() < 2)
          hb += " ";
        b.append(hb).append(" ");
      }
      throw new AtcoCifException(
          "Excepted feed header to start with ATCO-CIF.  Instead, found \""
              + start + "\" (" + b.toString() + ")");
    }
  }

  @Override
  protected boolean handleRecordType(Type type, ContentHandler handler) {
    switch (type) {
      case JOURNEY_HEADER:
        parseJourneyHeader(handler);
        break;
      case JOURNEY_DATE_RUNNING:
        parseJourneyDateRunning(handler);
        break;
      case JOURNEY_ORIGIN:
        parseJourneyOrigin(handler);
        break;
      case JOURNEY_INTERMEDIATE:
        parseJourneyIntermediate(handler);
        break;
      case JOURNEY_DESTINATION:
        parseJourneyDestination(handler);
        break;
      case LOCATION:
        parseLocation(handler);
        break;
      case ADDITIONAL_LOCATION:
        parseAdditionalLocation(handler);
        break;
      case VEHICLE_TYPE:
        parseVehicleType(handler);
        break;
      case ROUTE_DESCRIPTION:
        parseRouteDescription(handler);
        break;
      case OPERATOR:
        parseOperator(handler);
        break;
      case UNKNOWN:
        break;
      default:
        throw new AtcoCifException("unhandled record type: " + type);
    }
    return true;
  }

  private void parseJourneyHeader(ContentHandler handler) {
    JourneyHeaderElement element = element(new JourneyHeaderElement());

    String transactionType = pop(1);
    element.setOperatorId(pop(4));
    element.setJourneyIdentifier(pop(6));
    element.setStartDate(serviceDate(pop(8)));
    element.setEndDate(serviceDate(pop(8)));
    element.setMonday(integer(pop(1)));
    element.setTuesday(integer(pop(1)));
    element.setWednesday(integer(pop(1)));
    element.setThursday(integer(pop(1)));
    element.setFriday(integer(pop(1)));
    element.setSaturday(integer(pop(1)));

    element.setSunday(integer(pop(1)));

    String schoolTermTime = pop(1);
    String bankHolidays = pop(1);
    element.setRouteIdentifier(pop(4));
    String runningBoard = pop(6);

    element.setVehicleType(pop(8));

    String registrationNumber = pop(8);
    element.setRouteDirection(pop(1));

    closeCurrentJourneyIfNeeded(element, handler);
    _currentJourney = element;
    handler.startElement(element);
  }

  private void parseJourneyDateRunning(ContentHandler handler) {
    JourneyDateRunningElement element = element(new JourneyDateRunningElement());
    element.setStartDate(serviceDate(pop(8)));
    element.setEndDate(serviceDate(pop(8)));
    element.setOperationCode(integer(pop(1)));
    if (_currentJourney == null)
      throw new AtcoCifException("journey timepoint without header at "
          + describeLineLocation());
    _currentJourney.getCalendarModifications().add(element);
    fireElement(element, handler);

  }

  private void parseJourneyOrigin(ContentHandler handler) {
    JourneyOriginElement element = element(new JourneyOriginElement());
    element.setLocationId(pop(12));
    element.setDepartureTime(time(pop(4)));
    pushTimepointElement(element, handler);
  }

  private void parseJourneyIntermediate(ContentHandler handler) {
    JourneyIntermediateElement element = element(new JourneyIntermediateElement());
    element.setLocationId(pop(12));
    element.setArrivalTime(time(pop(4)));
    element.setDepartureTime(time(pop(4)));
    pushTimepointElement(element, handler);
  }

  private void parseJourneyDestination(ContentHandler handler) {
    JourneyDestinationElement element = element(new JourneyDestinationElement());
    element.setLocationId(pop(12));
    element.setArrivalTime(time(pop(4)));
    pushTimepointElement(element, handler);
  }

  private void pushTimepointElement(JourneyTimePointElement element,
      ContentHandler handler) {
    if (_currentJourney == null)
      throw new AtcoCifException("journey timepoint without header at "
          + describeLineLocation());
    element.setHeader(_currentJourney);
    _currentJourney.getTimePoints().add(element);
    fireElement(element, handler);
  }

  private void parseLocation(ContentHandler handler) {
    LocationElement element = element(new LocationElement());
    String transactionType = pop(1);
    element.setLocationId(pop(12));
    element.setName(pop(48));
    fireElement(element, handler);
  }

  private void parseAdditionalLocation(ContentHandler handler) {
    AdditionalLocationElement element = element(new AdditionalLocationElement());
    String transactionType = pop(1);
    element.setLocationId(pop(12));

    String xValue = pop(8);
    String yValue = pop(8);
    Point2D.Double location = getLocation(xValue, yValue, true);
    if (location != null) {
      element.setLat(location.y);
      element.setLon(location.x);
    }
    fireElement(element, handler);
  }

  private Point2D.Double getLocation(String xValue, String yValue,
      boolean canStripSuffix) {

    if (xValue.isEmpty() && yValue.isEmpty()) {
      return null;
    }

    Point2D.Double from = null;

    try {
      double x = Long.parseLong(xValue) / _locationScaleFactor;
      double y = Long.parseLong(yValue) / _locationScaleFactor;
      from = new Point2D.Double(x, y);
    } catch (NumberFormatException ex) {
      throw new AtcoCifException("error parsing additional location: x="
          + xValue + " y=" + yValue + " at " + describeLineLocation());
    }

    try {
      Point2D.Double result = new Point2D.Double();
      CoordinateSystemToCoordinateSystem.transform(_fromProjection,
          _toProjection, from, result);
      return result;
    } catch (ProjectionException ex) {
      _log.warn("error projecting additional location: x=" + xValue + " y="
          + yValue + " at " + describeLineLocation());
      return null;
    }
  }

  private void parseVehicleType(ContentHandler handler) {
    VehicleTypeElement element = element(new VehicleTypeElement());
    pop(1);
    element.setId(pop(8));
    element.setDescription(pop(24));
    fireElement(element, handler);
  }

  private void parseRouteDescription(ContentHandler handler) {
    RouteDescriptionElement element = element(new RouteDescriptionElement());
    pop(1);
    element.setOperatorId(pop(4));
    element.setRouteNumber(pop(4));
    element.setRouteDirection(pop(1));
    element.setRouteDescription(pop(68));
    fireElement(element, handler);
  }

  private void parseOperator(ContentHandler handler) {
    OperatorElement element = element(new OperatorElement());
    pop(1);
    element.setOperatorId(pop(4));
    element.setShortFormName(pop(24));
    element.setLegalName(pop(48));
    element.setEnquiryPhone(pop(12));
    element.setContactPhone(pop(12));
    fireElement(element, handler);
  }

  private void closeCurrentJourneyIfNeeded(AtcoCifElement element,
      ContentHandler handler) {
    if ((element == null || !(element instanceof JourneyChildElement))
        && _currentJourney != null) {
      handler.endElement(_currentJourney);
      _currentJourney = null;
    }
  }

  private Date serviceDate(String value) {
    try {

      Date serviceDate = _serviceDateFormat.parse(value);
      if (serviceDate.after(_maxServiceDate)) {
        serviceDate = _maxServiceDate;
      }
      return serviceDate;
    } catch (ParseException e) {
      throw new AtcoCifException("error parsing service date \"" + value
          + "\" at " + describeLineLocation(), e);
    }
  }

  private int time(String pop) {
    int hour = integer(pop.substring(0, 2));
    int min = integer(pop.substring(2));
    return hour * 60 + min;
  }


}
