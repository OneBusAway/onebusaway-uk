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
package org.onebusaway.uk.atco_cif_to_gtfs_converter;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.schema.AnnotationDrivenEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.gtfs.impl.GtfsRelationalDaoImpl;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.model.calendar.ServiceDate;
import org.onebusaway.gtfs.serialization.GtfsEntitySchemaFactory;
import org.onebusaway.gtfs.serialization.GtfsWriter;
import org.onebusaway.gtfs_transformer.factory.EntityRetentionGraph;
import org.onebusaway.uk.atco_cif.AdditionalLocationElement;
import org.onebusaway.uk.atco_cif.AtcoCifException;
import org.onebusaway.uk.atco_cif.AtcoCifParser;
import org.onebusaway.uk.atco_cif.JourneyDateRunningElement;
import org.onebusaway.uk.atco_cif.JourneyHeaderElement;
import org.onebusaway.uk.atco_cif.JourneyTimePointElement;
import org.onebusaway.uk.atco_cif.LocationElement;
import org.onebusaway.uk.atco_cif.LocationProvider;
import org.onebusaway.uk.atco_cif.OperatorElement;
import org.onebusaway.uk.atco_cif.RouteDescriptionElement;
import org.onebusaway.uk.atco_cif.VehicleTypeElement;
import org.onebusaway.uk.atco_cif.extensions.NationalExpressLocationGeoDetailElement;
import org.onebusaway.uk.atco_cif.extensions.NationalExpressLocationNameElement;
import org.onebusaway.uk.atco_cif.extensions.NationalExpressOperatorElement;
import org.onebusaway.uk.atco_cif.extensions.NationalExpressRouteDetailsElement;
import org.onebusaway.uk.atco_cif.extensions.greater_manchester.GreaterManchesterTimetableRowListElement;
import org.onebusaway.uk.naptan.csv.NaPTANStop;
import org.onebusaway.uk.parser.ContentHandler;
import org.onebusaway.uk.parser.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AtcoCifToGtfsConverter {

  private static Logger _log = LoggerFactory.getLogger(AtcoCifToGtfsConverter.class);

  private static final int MINUTES_IN_DAY = 24 * 60;

  private AtcoCifParser _parser = new AtcoCifParser();

  private File _inputPath;

  private File _outputPath;

  private String _agencyId = "1";

  private String _agencyName = "Agency Name";

  private String _agencyTimezone = "Europe/London";

  private String _agencyLang = "en";

  private String _agencyUrl = "http://agency.gov/";

  private String _agencyPhone = "";

  private int _vehicleType = -1;

  private Map<AgencyAndId, List<JourneyHeaderElement>> _journeysById = new HashMap<AgencyAndId, List<JourneyHeaderElement>>();

  private Map<String, LocationElement> _locationById = new HashMap<String, LocationElement>();

  private Map<String, AdditionalLocationElement> _additionalLocationById = new HashMap<String, AdditionalLocationElement>();

  private Map<String, NaPTANStop> _stopsByAtcoId = new HashMap<String, NaPTANStop>();

  private Map<String, GreaterManchesterTimetableRowListElement> _greaterManchesterRowListsByLocationId = new HashMap<String, GreaterManchesterTimetableRowListElement>();

  private Map<String, NationalExpressLocationNameElement> _nxLocationNamesById = new HashMap<String, NationalExpressLocationNameElement>();

  private Map<String, NationalExpressLocationGeoDetailElement> _nxLocationGeoDetailById = new HashMap<String, NationalExpressLocationGeoDetailElement>();

  private Map<String, NationalExpressOperatorElement> _nxOperatorsById = new HashMap<String, NationalExpressOperatorElement>();

  private Map<String, VehicleTypeElement> _vehicleTypesById = new HashMap<String, VehicleTypeElement>();

  private Map<AgencyAndId, RouteMetadata> _routeMetadataById = new HashMap<AgencyAndId, RouteMetadata>();

  private Map<String, OperatorElement> _operatorsById = new HashMap<String, OperatorElement>();

  private Map<String, String> _serviceDateModificationSuffixByKey = new HashMap<String, String>();

  private GtfsRelationalDaoImpl _dao = new GtfsRelationalDaoImpl();

  private boolean _keepStopsWithNoLocationInfo = false;

  private Set<String> _pruneStopsWithPrefixes = Collections.emptySet();

  private boolean _keepTripsWithMissingStops = false;

  private List<String> _preferredDirectionIdsForRouteDetails = Collections.emptyList();

  private File _naptanCsvPath;

  private Set<String> _stopsWithNoLocationInfo = new HashSet<String>();

  private int _prunedStopsWithNoLocationInfoCount = 0;

  private int _prunedStopTimesCount = 0;

  private int _prunedTripsCount = 0;

  public AtcoCifParser getParser() {
    return _parser;
  }

  public void setInputPath(File inputPath) {
    _inputPath = inputPath;
  }

  public void setOutputPath(File outputPath) {
    _outputPath = outputPath;
  }

  public void setAgencyId(String agencyId) {
    _agencyId = agencyId;
  }

  public void setAgencyName(String agencyName) {
    _agencyName = agencyName;
  }

  public void setAgencyUrl(String agencyUrl) {
    _agencyUrl = agencyUrl;
  }

  public void setAgencyPhone(String agencyPhone) {
    _agencyPhone = agencyPhone;
  }

  public void setAgencyTimezone(String agencyTimezone) {
    _agencyTimezone = agencyTimezone;
  }

  public void setAgencyLang(String agencyLang) {
    _agencyLang = agencyLang;
  }

  public void setVehicleType(int vehicleType) {
    _vehicleType = vehicleType;
  }

  public void setKeepStopsWithNoLocationInfo(
      boolean keepStopsWithNoLocationInfo) {
    _keepStopsWithNoLocationInfo = keepStopsWithNoLocationInfo;
  }

  public void setPruneStopsWithPrefixes(Set<String> pruneStopsWithPrefixes) {
    _pruneStopsWithPrefixes = pruneStopsWithPrefixes;
  }

  public void setKeepTripsWithMissingStops(boolean keepTripsWithMissingStops) {
    _keepTripsWithMissingStops = keepTripsWithMissingStops;
  }

  public void setPreferredDirectionIdsForRouteDetails(List<String> ids) {
    _preferredDirectionIdsForRouteDetails = ids;
  }

  public void setNaptanCsvPath(File naptanCsvPath) {
    _naptanCsvPath = naptanCsvPath;
  }

  public void run() throws IOException {

    _log.info("Input path: " + _inputPath);
    _log.info("Output path: " + _outputPath);

    loadNaptanDataIfNeeded();

    List<File> paths = new ArrayList<File>();
    getApplicableFiles(_inputPath, paths);

    if (paths.isEmpty()) {
      _log.error("No applicable input files were found!");
      System.exit(-1);
    }

    HandlerImpl handler = new HandlerImpl();
    for (File path : paths) {
      _log.info("parsing file: " + path);
      _parser.parse(path, handler);
    }

    constructGtfs();
    writeGtfs();

    if (_prunedStopsWithNoLocationInfoCount > 0) {
      _log.info(String.format(
          "pruned stops with no location info: %d stops used by %d stop times in %d trips",
          _prunedStopsWithNoLocationInfoCount, _prunedStopTimesCount,
          _prunedTripsCount));
    }
  }

  private void loadNaptanDataIfNeeded() throws IOException {
    if (_naptanCsvPath == null) {
      return;
    }

    CsvEntityReader reader = new CsvEntityReader();
    reader.setInputLocation(_naptanCsvPath);
    AnnotationDrivenEntitySchemaFactory entitySchema = new AnnotationDrivenEntitySchemaFactory();
    entitySchema.addPackageToScan("org.onebusaway.uk.naptan.csv");
    reader.setEntitySchemaFactory(entitySchema);
    reader.addEntityHandler(new EntityHandler() {
      @Override
      public void handleEntity(Object arg0) {
        NaPTANStop stop = (NaPTANStop) arg0;
        _stopsByAtcoId.put(stop.getAtcoCode(), stop);
      }
    });
    reader.readEntities(NaPTANStop.class);

    reader.close();

  }

  private void constructGtfs() {
    constructTrips();
    pruneTrips();
  }

  private void constructTrips() {
    for (List<JourneyHeaderElement> journies : _journeysById.values()) {
      for (int i = 0; i < journies.size(); ++i) {
        JourneyHeaderElement journey = journies.get(i);
        if (journey.getOperatorId().equals("EU")) {
          continue;
        }
        Trip trip = new Trip();
        String id = journey.getOperatorId() + "-"
            + journey.getJourneyIdentifier();
        if (journies.size() > 1) {
          id += "-" + i;
        }
        trip.setId(new AgencyAndId(journey.getOperatorId(), id));
        trip.setRoute(getRouteForJourney(journey));
        trip.setServiceId(getServiceIdForJourney(journey));

        AgencyAndId routeId = trip.getRoute().getId();
        RouteMetadata metadata = getMetadataForRouteId(routeId);

        String directionName = metadata.getDirectionNameForDirectionId(journey.getRouteDirection());
        if (!isEmpty(directionName)) {
          trip.setTripHeadsign(directionName);
        }
        Integer directionId = metadata.getDirectionIdForDirection(journey.getRouteDirection());
        if (directionId != null) {
          trip.setDirectionId(directionId.toString());
        }

        if (constructTimepoints(journey, trip)) {
          _dao.saveEntity(trip);
        }
      }
    }
  }

  private RouteMetadata getMetadataForRouteId(AgencyAndId routeId) {
    RouteMetadata metadata = _routeMetadataById.get(routeId);
    if (metadata == null) {
      metadata = new RouteMetadata();
      _routeMetadataById.put(routeId, metadata);
    }
    return metadata;
  }

  @SuppressWarnings("unchecked")
  private void pruneTrips() {
    EntityRetentionGraph graph = new EntityRetentionGraph(_dao);
    for (Trip trip : _dao.getAllTrips()) {
      List<StopTime> stopTimes = _dao.getStopTimesForTrip(trip);
      if (stopTimes.size() > 1) {
        graph.retainUp(trip);
      }
    }
    for (Class<?> entityClass : GtfsEntitySchemaFactory.getEntityClasses()) {
      List<Object> objectsToRemove = new ArrayList<Object>();
      for (Object entity : _dao.getAllEntitiesForType(entityClass)) {
        if (!graph.isRetained(entity))
          objectsToRemove.add(entity);
      }
      for (Object toRemove : objectsToRemove)
        _dao.removeEntity((IdentityBean<Serializable>) toRemove);
    }
  }

  private Route getRouteForJourney(JourneyHeaderElement journey) {
    AgencyAndId routeId = getRouteIdForJourney(journey);
    Route route = _dao.getRouteForId(routeId);
    if (route == null) {
      route = new Route();
      route.setAgency(getAgencyForId(routeId.getAgencyId()));
      route.setId(routeId);
      route.setShortName(journey.getRouteIdentifier());
      route.setType(getRouteTypeForJourney(journey));
      RouteMetadata metaData = _routeMetadataById.get(routeId);
      if (metaData != null) {
        for (String id : _preferredDirectionIdsForRouteDetails) {
          String longName = metaData.getRouteLongNameForDirectionId(id);
          if (longName != null) {
            route.setLongName(longName);
            break;
          }
        }
        for (String id : _preferredDirectionIdsForRouteDetails) {
          String routeUrl = metaData.getRouteUrlForDirectionId(id);
          if (routeUrl != null) {
            route.setUrl(routeUrl);
            break;
          }
        }
      }
      _dao.saveEntity(route);
    }
    return route;
  }

  private AgencyAndId getRouteIdForJourney(JourneyHeaderElement journey) {
    return getRouteId(journey.getOperatorId(), journey.getRouteIdentifier());
  }

  private AgencyAndId getRouteId(String operatorId, String routeId) {
    // Note that we include the operator id in the id portion as well
    // because
    // the route identifiers are not unique by themselves in the output
    // GTFS.
    return new AgencyAndId(operatorId, operatorId + "-" + routeId);
  }

  private Agency getAgencyForId(String id) {
    Agency agency = _dao.getAgencyForId(id);
    if (agency == null) {
      agency = new Agency();
      agency.setId(id);
      agency.setTimezone(_agencyTimezone);
      agency.setLang(_agencyLang);

      OperatorElement operator = _operatorsById.get(id);
      if (operator != null) {
        if (!isEmpty(operator.getShortFormName())) {
          agency.setName(operator.getShortFormName());
        }
        if (!isEmpty(operator.getEnquiryPhone())) {
          agency.setPhone(operator.getEnquiryPhone());
        }
      }
      NationalExpressOperatorElement nxOperator = _nxOperatorsById.get(id);
      if (nxOperator != null) {
        if (!isEmpty(nxOperator.getMarketingName())) {
          agency.setName(nxOperator.getMarketingName());
        }
        if (!isEmpty(nxOperator.getUrl())) {
          agency.setUrl(nxOperator.getUrl());
        }
      }

      if (isEmpty(agency.getName())) {
        agency.setName(_agencyName);
      }
      if (isEmpty(agency.getPhone())) {
        agency.setPhone(_agencyPhone);
      }
      if (isEmpty(agency.getUrl())) {
        agency.setUrl(_agencyUrl);
      }
      _dao.saveEntity(agency);
    }
    return agency;
  }

  private int getRouteTypeForJourney(JourneyHeaderElement journey) {
    String vehicleType = journey.getVehicleType();
    VehicleTypeElement vehicleTypeElement = _vehicleTypesById.get(journey.getVehicleType());

    if (vehicleTypeElement != null) {
      vehicleType = vehicleTypeElement.getDescription();
    }
    String desc = vehicleType.toLowerCase();
    if (desc.equals("bus") || desc.equals("coach")) {
      return 3;
    } else if (desc.equals("heavy rail")) {
      return 2;
    } else if (_vehicleType != -1) {
      return _vehicleType;
    } else {
      throw new AtcoCifException(
          "no defautl VehicleType specified and could not determine GTFS route vehicle type from ATCO-CIF vehicle type description: "
              + desc);
    }
  }

  private AgencyAndId getServiceIdForJourney(JourneyHeaderElement journey) {
    AgencyAndId serviceId = constructServiceIdForJourney(journey);
    ServiceCalendar calendar = _dao.getCalendarForServiceId(serviceId);
    if (calendar == null) {
      calendar = new ServiceCalendar();
      calendar.setServiceId(serviceId);
      calendar.setStartDate(getServiceDate(journey.getStartDate()));
      calendar.setEndDate(getServiceDate(journey.getEndDate()));
      calendar.setMonday(journey.getMonday());
      calendar.setTuesday(journey.getTuesday());
      calendar.setWednesday(journey.getWednesday());
      calendar.setThursday(journey.getThursday());
      calendar.setFriday(journey.getFriday());
      calendar.setSaturday(journey.getSaturday());
      calendar.setSunday(journey.getSunday());
      _dao.saveEntity(calendar);

      for (JourneyDateRunningElement modification : journey.getCalendarModifications()) {
        Date startDate = modification.getStartDate();
        Date endDate = modification.getEndDate();

        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(startDate);

        int exceptionType = modification.getOperationCode() == 1 ? 1 : 2;

        while (true) {
          Date date = c.getTime();
          if (date.after(endDate))
            break;

          ServiceCalendarDate calendarDate = new ServiceCalendarDate();
          calendarDate.setServiceId(serviceId);
          calendarDate.setDate(new ServiceDate(date));
          calendarDate.setExceptionType(exceptionType);
          _dao.saveEntity(calendarDate);

          c.add(Calendar.DAY_OF_YEAR, 1);
        }
      }
      _dao.clearAllCaches();
    }
    return serviceId;
  }

  private ServiceDate getServiceDate(Date date) {
    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
    c.setTime(date);
    return new ServiceDate(c);
  }

  private AgencyAndId constructServiceIdForJourney(JourneyHeaderElement journey) {
    StringBuilder b = new StringBuilder();
    b.append(getServiceDate(journey.getStartDate()).getAsString());
    b.append('-');
    b.append(getServiceDate(journey.getEndDate()).getAsString());
    b.append('-');
    b.append(journey.getSunday() == 1 ? "S" : "_");
    b.append(journey.getMonday() == 1 ? "M" : "_");
    b.append(journey.getTuesday() == 1 ? "T" : "_");
    b.append(journey.getWednesday() == 1 ? "W" : "_");
    b.append(journey.getThursday() == 1 ? "H" : "_");
    b.append(journey.getFriday() == 1 ? "F" : "_");
    b.append(journey.getSaturday() == 1 ? "S" : "_");
    b.append('-');
    b.append(getServiceDateModificationsSuffix(journey));
    return id(b.toString());
  }

  private String getServiceDateModificationsSuffix(JourneyHeaderElement journey) {
    List<JourneyDateRunningElement> modifications = journey.getCalendarModifications();
    if (modifications.isEmpty()) {
      return "00";
    }

    StringBuilder b = new StringBuilder();
    Collections.sort(modifications);
    for (JourneyDateRunningElement modification : modifications) {
      b.append('|');
      b.append(getServiceDate(modification.getStartDate()).getAsString());
      b.append('-');
      b.append(getServiceDate(modification.getEndDate()).getAsString());
      b.append('-');
      b.append(modification.getOperationCode());
    }
    String key = b.toString();
    String suffix = _serviceDateModificationSuffixByKey.get(key);
    if (suffix == null) {
      DecimalFormat format = new DecimalFormat("00");
      suffix = format.format(_serviceDateModificationSuffixByKey.size() + 1);
      _serviceDateModificationSuffixByKey.put(key, suffix);
    }
    return suffix;
  }

  private boolean constructTimepoints(JourneyHeaderElement journey, Trip trip) {

    normalizeTimes(journey);

    boolean first = true;

    for (JourneyTimePointElement timePoint : journey.getTimePoints()) {
      String stopId = timePoint.getLocationId();
      Stop stop = findStop(stopId);
      if (stop == null && !_keepTripsWithMissingStops) {
        _prunedTripsCount++;
        return false;
      }
    }

    for (JourneyTimePointElement timePoint : journey.getTimePoints()) {
      String stopId = timePoint.getLocationId();
      Stop stop = findStop(stopId);

      /**
       * A NULL stop indicates a stop that has been pruned because it doesn't
       * have location information. We do not produce stop times for these
       * stops.
       */
      if (stop == null) {
        _prunedStopTimesCount++;
        continue;
      }

      StopTime stopTime = new StopTime();
      stopTime.setTrip(trip);
      stopTime.setStop(stop);
      if (timePoint.getArrivalTime() != 0 || timePoint.getDepartureTime() != 0
          || first) {
        stopTime.setArrivalTime(timePoint.getArrivalTime() * 60);
        stopTime.setDepartureTime(timePoint.getDepartureTime() * 60);
      }
      if (!timePoint.isPickUpAllowed()) {
        stopTime.setPickupType(1);
      }
      if (!timePoint.isDropOffAllowed()) {
        stopTime.setDropOffType(1);
      }

      stopTime.setStopSequence(_dao.getAllStopTimes().size());
      _dao.saveEntity(stopTime);
      first = false;
    }
    return true;
  }

  private void normalizeTimes(JourneyHeaderElement journey) {
    List<JourneyTimePointElement> timepoints = journey.getTimePoints();
    if (timepoints.isEmpty()) {
      return;
    }

    int prevDepartureTime = -1;
    int dayOffset = 0;

    for (int i = 0; i < timepoints.size(); ++i) {
      JourneyTimePointElement timepoint = timepoints.get(i);
      int arrivalTime = timepoint.getArrivalTime();
      int departureTime = timepoint.getDepartureTime();

      arrivalTime += dayOffset * MINUTES_IN_DAY;
      while (arrivalTime < prevDepartureTime) {
        arrivalTime += MINUTES_IN_DAY;
        dayOffset++;
      }

      departureTime += dayOffset * MINUTES_IN_DAY;
      while (departureTime < arrivalTime) {
        departureTime += MINUTES_IN_DAY;
        dayOffset++;
      }

      timepoint.setArrivalTime(arrivalTime);
      timepoint.setDepartureTime(departureTime);
      prevDepartureTime = departureTime;
    }
  }

  private Stop findStop(String stopId) {

    for (String prefix : _pruneStopsWithPrefixes) {
      if (stopId.startsWith(prefix)) {
        return null;
      }
    }
    NaPTANStop naptanStop = _stopsByAtcoId.get(stopId);
    if (naptanStop != null) {
      return getNaptanStop(naptanStop);
    }
    LocationElement location = getLocationForId(stopId);
    if (location != null) {
      return getAtcoStop(location);
    }
    GreaterManchesterTimetableRowListElement rowList = _greaterManchesterRowListsByLocationId.get(stopId);
    if (rowList != null) {
      return getGreaterManchesterRowListStop(rowList);
    }
    throw new AtcoCifException("no stop found with id " + stopId);
  }

  private Stop getNaptanStop(NaPTANStop naptanStop) {
    AgencyAndId id = id(naptanStop.getAtcoCode());
    Stop stop = _dao.getStopForId(id);
    if (stop == null) {
      stop = new Stop();
      stop.setId(id);
      stop.setName(naptanStop.getCommonName());
      stop.setLat(naptanStop.getLatitude());
      stop.setLon(naptanStop.getLongitude());
      _dao.saveEntity(stop);
    }
    return stop;
  }

  private LocationElement getLocationForId(String stopId) {
    LocationElement location = _locationById.get(stopId);
    /**
     * I've noticed a strange case where a journey references a stop with an id
     * "blahX" when only a stop with id "blah" exists.
     */
    if (location == null) {
      if (stopId.length() > 1) {
        stopId = stopId.substring(0, stopId.length() - 1);
        location = _locationById.get(stopId);
      }
    }
    return location;
  }

  private Stop getAtcoStop(LocationElement location) {
    String locationId = location.getLocationId();
    AgencyAndId id = id(locationId);
    Stop stop = _dao.getStopForId(id);
    if (stop == null) {
      LocationProvider locationSource = _additionalLocationById.get(locationId);
      if (locationSource == null) {
        throw new AtcoCifException("found location with id=" + locationId
            + " but no additional location information found");
      }

      LocationProvider nxLocationSource = _nxLocationGeoDetailById.get(locationId);
      if (nxLocationSource != null) {
        locationSource = nxLocationSource;
      }

      if (locationSource.getLat() == 0.0 || locationSource.getLon() == 0.0) {
        if (_stopsWithNoLocationInfo.add(locationId)) {
          _log.info("stop with no location: " + locationId);
          _prunedStopsWithNoLocationInfoCount++;
        }
        if (!_keepStopsWithNoLocationInfo) {
          return null;
        }
      }
      String name = location.getName();
      NationalExpressLocationNameElement nxNameElement = _nxLocationNamesById.get(locationId);
      if (nxNameElement != null && !nxNameElement.getShortName().isEmpty()) {
        name = nxNameElement.getShortName();
      }

      stop = new Stop();
      stop.setId(id(locationId));
      stop.setName(name);
      stop.setLat(locationSource.getLat());
      stop.setLon(locationSource.getLon());

      _dao.saveEntity(stop);
    }
    return stop;
  }

  private Stop getGreaterManchesterRowListStop(
      GreaterManchesterTimetableRowListElement rowList) {
    AgencyAndId id = id(rowList.getLocationReference());
    Stop stop = _dao.getStopForId(id);
    if (stop == null) {
      stop = new Stop();
      stop.setId(id);
      stop.setName(rowList.getFullLocation());
      _dao.saveEntity(stop);
    }
    return stop;
  }

  private void writeGtfs() throws IOException {
    GtfsWriter writer = new GtfsWriter();
    writer.setOutputLocation(_outputPath);
    DefaultEntitySchemaFactory schemaFactory = new DefaultEntitySchemaFactory();
    schemaFactory.addFactory(GtfsEntitySchemaFactory.createEntitySchemaFactory());
    writer.setEntitySchemaFactory(schemaFactory);
    writer.run(_dao);
  }

  private void getApplicableFiles(File path, List<File> applicableFiles) {
    _log.info("Scanning path: " + path);
    if (path.isDirectory()) {
      _log.info("Directory found...");
      for (File subPath : path.listFiles()) {
        getApplicableFiles(subPath, applicableFiles);
      }
    } else if (path.getName().toLowerCase().endsWith(".cif")) {
      _log.info("CIF File found!");
      applicableFiles.add(path);
    }
  }

  private AgencyAndId id(String id) {
    return new AgencyAndId(_agencyId, id);
  }

  private static final boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }

  private class HandlerImpl implements ContentHandler {

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {

    }

    @Override
    public void startElement(Element element) {
      if (element instanceof JourneyHeaderElement) {
        JourneyHeaderElement journey = (JourneyHeaderElement) element;
        AgencyAndId journeyId = new AgencyAndId(journey.getOperatorId(),
            journey.getJourneyIdentifier());
        List<JourneyHeaderElement> journies = _journeysById.get(journeyId);
        if (journies == null) {
          journies = new ArrayList<JourneyHeaderElement>();
          _journeysById.put(journeyId, journies);
        }
        journies.add(journey);
        AgencyAndId routeId = getRouteIdForJourney(journey);
        RouteMetadata metadata = getMetadataForRouteId(routeId);
        metadata.addDirection(journey.getRouteDirection());
      } else if (element instanceof LocationElement) {
        LocationElement location = (LocationElement) element;
        _locationById.put(location.getLocationId(), location);
      } else if (element instanceof AdditionalLocationElement) {
        AdditionalLocationElement location = (AdditionalLocationElement) element;
        _additionalLocationById.put(location.getLocationId(), location);
      } else if (element instanceof VehicleTypeElement) {
        VehicleTypeElement vehicle = (VehicleTypeElement) element;
        _vehicleTypesById.put(vehicle.getId(), vehicle);
      } else if (element instanceof RouteDescriptionElement) {
        RouteDescriptionElement route = (RouteDescriptionElement) element;
        AgencyAndId id = new AgencyAndId(route.getOperatorId(),
            route.getOperatorId() + "-" + route.getRouteNumber());
        RouteMetadata metadata = getMetadataForRouteId(id);
        metadata.addRouteDescription(route);
      } else if (element instanceof OperatorElement) {
        OperatorElement operator = (OperatorElement) element;
        OperatorElement existing = _operatorsById.put(operator.getOperatorId(),
            operator);
        if (existing != null) {
          _log.info("!");
        }
      } else if (element instanceof NationalExpressLocationNameElement) {
        NationalExpressLocationNameElement nxNameElement = (NationalExpressLocationNameElement) element;
        _nxLocationNamesById.put(nxNameElement.getLocationId(), nxNameElement);
      } else if (element instanceof NationalExpressLocationGeoDetailElement) {
        NationalExpressLocationGeoDetailElement nxGeoDetailElement = (NationalExpressLocationGeoDetailElement) element;
        _nxLocationGeoDetailById.put(nxGeoDetailElement.getLocationId(),
            nxGeoDetailElement);
      } else if (element instanceof NationalExpressOperatorElement) {
        NationalExpressOperatorElement nxElement = (NationalExpressOperatorElement) element;
        _nxOperatorsById.put(nxElement.getId(), nxElement);
      } else if (element instanceof NationalExpressRouteDetailsElement) {
        NationalExpressRouteDetailsElement details = (NationalExpressRouteDetailsElement) element;
        AgencyAndId routeId = getRouteId(details.getOperatorId(),
            details.getRouteId());
        RouteMetadata metadata = getMetadataForRouteId(routeId);
        metadata.addNXRouteDetails(details);
      } else if (element instanceof GreaterManchesterTimetableRowListElement) {
        GreaterManchesterTimetableRowListElement rowList = (GreaterManchesterTimetableRowListElement) element;
        _greaterManchesterRowListsByLocationId.put(
            rowList.getLocationReference(), rowList);
      }
    }

    @Override
    public void endElement(Element element) {

    }
  }
}
