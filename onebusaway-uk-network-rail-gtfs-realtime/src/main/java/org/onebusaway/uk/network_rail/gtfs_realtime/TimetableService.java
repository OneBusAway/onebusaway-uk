package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.onebusaway.csv_entities.CsvEntityReader;
import org.onebusaway.csv_entities.EntityHandler;
import org.onebusaway.csv_entities.exceptions.CsvEntityIOException;
import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.atoc.timetable_parser.TimetableBundle;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TiplocInsertElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.StationLocation;
import org.onebusaway.uk.parser.DefaultContentHandler;
import org.onebusaway.uk.parser.Element;
import org.onebusaway.uk.parser.ProjectionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TimetableService {

  private static final Logger _log = LoggerFactory.getLogger(TimetableService.class);

  private StatisticsService _statisticsService;

  private Map<String, StationElement> _stationsByTiploc = new HashMap<String, StationElement>();

  private Map<Integer, Set<String>> _tiplocsByStanox = new HashMap<Integer, Set<String>>();

  private Map<String, Integer> _stanoxByTiplocs = new HashMap<String, Integer>();

  private Map<String, BasicScheduleElement> _schedulesByTrainUid = new HashMap<String, BasicScheduleElement>();

  private Map<String, Set<String>> _berthAreaToStanoxAreas = new HashMap<String, Set<String>>();

  private Map<String, StationLocation> _stationLocationsByTiploc = new HashMap<String, StationLocation>();

  @Inject
  public void setStatisticsService(StatisticsService statisticsService) {
    _statisticsService = statisticsService;
  }

  public Collection<Integer> getAllStanox() {
    return Collections.unmodifiableCollection(_tiplocsByStanox.keySet());
  }

  public Set<String> getTiplocsForStanox(int stanox) {
    Set<String> tiplocs = _tiplocsByStanox.get(stanox);
    if (tiplocs == null) {
      tiplocs = Collections.emptySet();
    }
    return tiplocs;
  }

  public int getStanoxForTiploc(String tiploc) {
    Integer stanox = _stanoxByTiplocs.get(tiploc);
    if (stanox == null) {
      return 0;
    }
    return stanox;
  }

  public StationElement getStationForTiploc(String tiploc) {
    return _stationsByTiploc.get(tiploc);
  }

  public StationLocation getStationLocationForTiploc(String tiploc) {
    return _stationLocationsByTiploc.get(tiploc);
  }

  public BasicScheduleElement getBestScheduleForTrainUid(String trainUid) {
    BasicScheduleElement schedule = _schedulesByTrainUid.get(trainUid);
    if (schedule == null) {
      _statisticsService.incrementUnknownTrainUidCount();
    }
    return schedule;
  }

  public String getAreaForStanox(int stanox) {
    String value = Integer.toString(stanox);
    return value.substring(0, 2);
  }

  public Set<String> getStanoxAreasForBerthArea(String berthAreaId) {
    Set<String> stanoxAreaIds = _berthAreaToStanoxAreas.get(berthAreaId);
    if (stanoxAreaIds == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(stanoxAreaIds);
  }

  public Point2D.Double getBestLocationForStanox(int stanox) {
    double xTotal = 0;
    double yTotal = 0;
    int count = 0;

    for (String tiploc : getTiplocsForStanox(stanox)) {
      StationLocation stationLocation = getStationLocationForTiploc(tiploc);
      if (stationLocation != null) {
        xTotal += stationLocation.getX();
        yTotal += stationLocation.getY();
        count++;
        continue;
      }
      StationElement stationElement = getStationForTiploc(tiploc);
      if (stationElement != null) {
        xTotal += stationElement.getEasting();
        yTotal += stationElement.getNorthing();
        count++;
        continue;
      }
    }
    if (count == 0) {
      return null;
    }
    return new Point2D.Double(xTotal / count, yTotal / count);
    // TODO Auto-generated method stub

  }

  public void readScheduleData(File path) throws IOException {
    TimetableBundle bundle = new TimetableBundle(path);
    _log.info("loading master station names");
    bundle.readMasterStationNames(new MasterStationNameHandler());
    _log.info("loading timetable");
    bundle.readTimetable(new ScheduleHandler());
    _log.info("load complete");
  }

  public void readBerthToStanoxAreaMapping(File path) throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(path));
    try {
      // Skip first line
      String line = reader.readLine();
      if (!line.equals("berth_area,stanox_area")) {
        throw new IllegalStateException(
            "missing expected header for berth-to-stanox-area mapping file: expected=berth_area,stanox_area actual="
                + line);
      }
      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(",");
        if (tokens.length != 2) {
          throw new IllegalStateException("malformed line=" + line);
        }
        String berthAreaId = tokens[0];
        String stanoxAreaId = tokens[1];
        Set<String> stanoxAreaIds = _berthAreaToStanoxAreas.get(berthAreaId);
        if (stanoxAreaIds == null) {
          stanoxAreaIds = new HashSet<String>();
          _berthAreaToStanoxAreas.put(berthAreaId, stanoxAreaIds);
        }
        stanoxAreaIds.add(stanoxAreaId);
      }
    } finally {
      reader.close();
    }
  }

  public void readStationLocations(File path) throws CsvEntityIOException,
      IOException {
    BufferedReader reader = new BufferedReader(new FileReader(path));
    CsvEntityReader csvEntityReader = new CsvEntityReader();
    csvEntityReader.addEntityHandler(new EntityHandler() {
      @Override
      public void handleEntity(Object bean) {
        StationLocation stationLocation = (StationLocation) bean;
        Point2D.Double xy = ProjectionSupport.convertFromLatLon(
            stationLocation.getLat(), stationLocation.getLon());
        stationLocation.setX(xy.x);
        stationLocation.setY(xy.y);
        _stationLocationsByTiploc.put(stationLocation.getTiploc(),
            stationLocation);
      }
    });
    csvEntityReader.readEntities(StationLocation.class, reader);
    reader.close();
  }

  private class MasterStationNameHandler extends DefaultContentHandler {

    @Override
    public void startElement(Element element) {
      if (element instanceof StationElement) {
        StationElement station = (StationElement) element;
        StationElement existing = _stationsByTiploc.put(station.getTiploc(),
            station);
        if (existing != null) {
          _log.warn("duplicate station tiploc=" + station.getTiploc());
        }
      }
    }
  }

  private class ScheduleHandler extends DefaultContentHandler {

    @Override
    public void endElement(Element element) {
      if (element instanceof TiplocInsertElement) {
        TiplocInsertElement insert = (TiplocInsertElement) element;
        if (insert.getStanox() == 0) {
          return;
        }
        _stanoxByTiplocs.put(insert.getTiploc(), insert.getStanox());
        Set<String> tiplocs = _tiplocsByStanox.get(insert.getStanox());
        if (tiplocs == null) {
          tiplocs = new HashSet<String>();
          _tiplocsByStanox.put(insert.getStanox(), tiplocs);
        }
        tiplocs.add(insert.getTiploc());
      }
      if (element instanceof BasicScheduleElement) {
        BasicScheduleElement schedule = (BasicScheduleElement) element;
        if (!_schedulesByTrainUid.containsKey(schedule.getTrainUid())) {
          _schedulesByTrainUid.put(schedule.getTrainUid(), schedule);
        }
      }
    }
  }

}
