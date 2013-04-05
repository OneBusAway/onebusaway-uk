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
package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.onebusaway.status_exporter.StatusProviderService;
import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.atoc.timetable_parser.TimetableBundle;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TimepointElement;
import org.onebusaway.uk.network_rail.cif.TiplocInsertElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainInstance;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthCancelMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthHeartbeatMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthInterposeMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthStepMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainDescriberMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainMovementBody;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainMovementMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainState;
import org.onebusaway.uk.parser.DefaultContentHandler;
import org.onebusaway.uk.parser.Element;
import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeLibrary;
import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeMutableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.transit.realtime.GtfsRealtime.FeedEntity;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import com.google.transit.realtime.GtfsRealtime.TripDescriptor;
import com.google.transit.realtime.GtfsRealtime.TripUpdate;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeEvent;
import com.google.transit.realtime.GtfsRealtime.TripUpdate.StopTimeUpdate;
import com.google.transit.realtime.GtfsRealtime.VehicleDescriptor;

@Singleton
public class GtfsRealtimeService implements StatusProviderService,
    MessageHandler {

  private static final Logger _log = LoggerFactory.getLogger(GtfsRealtimeService.class);

  private static final DateFormat _dateFormat = new SimpleDateFormat(
      "yyyy-MM-dd_HH-mm-ss");

  private Gson _gson;

  private GtfsRealtimeMutableProvider _gtfsRealtimeProvider;

  private StatisticsService _statisticsService;

  private NarrativeService _narrativeService;

  private Map<String, StationElement> _stationsByTiploc = new HashMap<String, StationElement>();

  private Map<String, TiplocInsertElement> _insertsByTiploc = new HashMap<String, TiplocInsertElement>();

  private Map<String, BasicScheduleElement> _schedulesByTrainUid = new HashMap<String, BasicScheduleElement>();

  private Map<String, TrainInstance> _trainsById = new HashMap<String, TrainInstance>();

  private Map<String, Set<TrainInstance>> _trainsByShortId = new HashMap<String, Set<TrainInstance>>();

  private Map<String, Integer> _stanoxByBerthId = new HashMap<String, Integer>();

  private long _mostRecentTimestamp = 0;

  private File _atocTimetablePath;

  private File _naptanPath;

  private File _berthMappingPath;

  private File _statePath;

  private boolean _inReplay = false;

  /**
   * If it's been more than the specified number of seconds since we've heard an
   * activation update from a train, we remove it from the list of currently
   * tracked trains.
   */
  private int _trainActivationExpirationTimeInSeconds = 3 * 60 * 60;

  /**
   * If it's been more than the specified number of seconds since we've heard a
   * movement update from a train, we remove it from the list of currently
   * tracked trains.
   */
  private int _trainMovemenExpirationTimeInSeconds = 60 * 60;

  @Inject
  public void setGson(Gson gson) {
    _gson = gson;
  }

  @Inject
  public void setGtfsRealtimeProvider(GtfsRealtimeMutableProvider provider) {
    _gtfsRealtimeProvider = provider;
  }

  @Inject
  public void setStatisticsService(StatisticsService statisticsService) {
    _statisticsService = statisticsService;
  }

  @Inject
  public void setNarrativeService(NarrativeService narrativeService) {
    _narrativeService = narrativeService;
  }

  public void setAtocTimetablePath(File atocTimetablePath) {
    _atocTimetablePath = atocTimetablePath;
  }

  public void setNaptanPath(File naptanPath) {
    _naptanPath = naptanPath;
  }

  public void setBerthMappingPath(File berthMappingPath) {
    _berthMappingPath = berthMappingPath;
  }

  public void setStatePath(File statePath) {
    _statePath = statePath;
  }

  @PostConstruct
  public void start() throws IOException, JAXBException {

    TimetableBundle bundle = new TimetableBundle(_atocTimetablePath);
    _log.info("loading master station names");
    bundle.readMasterStationNames(new MasterStationNameHandler());
    _log.info("loading timetable");
    bundle.readTimetable(new ScheduleHandler());
    _log.info("load complete");

    if (_naptanPath != null) {
      _log.info("creating JAXB context");
      JAXBContext context = JAXBContext.newInstance("uk.org.naptan");
      Unmarshaller unmarshaller = context.createUnmarshaller();
      _log.info("unmarshalling NAPTAN database");
      Object result = unmarshaller.unmarshal(_naptanPath);
      _log.info("result=" + result);
    }

    if (_berthMappingPath != null) {
      BufferedReader reader = new BufferedReader(new FileReader(
          _berthMappingPath));
      String line = null;
      while ((line = reader.readLine()) != null) {
        String[] tokens = line.split(",");
        String areaId = tokens[0];
        String from = tokens[2];
        String to = tokens[3];
        int stanox = Integer.parseInt(tokens[4]);
        _stanoxByBerthId.put(areaId + "_" + from, stanox);
        _stanoxByBerthId.put(areaId + "_" + to, stanox);
      }
      reader.close();
    }
    if (_statePath != null) {
      reloadState();
    }

    updateFeeds();

    _log.info("startup complete");
  }

  @Override
  public void processMessage(long timestamp, EMessageType messageType, String jsonMessage, String source)
      throws IOException {
    if (!jsonMessage.startsWith("[")) {
      return;
    }
    switch (messageType) {
      case TRAIN_MOVEMENT: {
        TrainMovementMessage[] messages = _gson.fromJson(jsonMessage,
            TrainMovementMessage[].class);
        for (TrainMovementMessage message : messages) {
          processTrainMovementMessage(message);
        }
        break;
      }
      case TD: {
        TrainDescriberMessage[] messages = _gson.fromJson(jsonMessage,
            TrainDescriberMessage[].class);
        for (TrainDescriberMessage message : messages) {
          processTrainDescriberMessage(message);
        }
      }
    }

    removeExpiredTrains();
    updateFeeds();
    saveState();
  }

  /****
   * {@link StatusProviderService} Interface
   ****/

  @Override
  public void getStatus(Map<String, String> status) {
    status.put("network_rail_gtfs_realtime.activeTrains",
        Integer.toString(_trainsById.size()));
    if (_mostRecentTimestamp != 0) {
      status.put("network_rail_gtfs_realtime.mostRecentTimestamp",
          _dateFormat.format(new Date(_mostRecentTimestamp)));
    }
  }

  /****
   * Private Methods
   ****/

  private void processTrainMovementMessage(TrainMovementMessage message) {

    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));

    String trainId = message.getBody().getTrainId();
    _statisticsService.incrementMessage(msgType, trainId);

    _mostRecentTimestamp = Long.parseLong(message.getHeader().getMsgQueueTimestamp());

    switch (msgType) {
      case ACTIVATION:
        handleActivation(message);
        break;
      case CANCELLATION:
        handleCancellation(message);
        break;
      case MOVEMENT:
        handleMovement(message);
        break;
      case UNIDENTIFIED_TRAIN:
        handleUnidentifiedTrain(message);
        break;
      case REINSTATEMENT:
        handleReinstatement(message);
        break;
      case CHANGE_OF_ORIGIN:
        handleChangeOfOrigin(message);
        break;
      case CHANGE_OF_IDENTITY:
        handleChangleOfIdentity(message);
        break;
      case CHANGE_OF_LOCATION:
        handleChangeOfLocation(message);
        break;
    }
  }

  private void handleActivation(TrainMovementMessage message) {
    TrainMovementBody body = message.getBody();
    BasicScheduleElement schedule = getBestScheduleForTrainUid(body.getTrainUid());
    if (schedule == null) {
      return;
    }
    String trainId = body.getTrainId();
    long timestamp = Long.parseLong(message.getHeader().getMsgQueueTimestamp());

    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
    c.setTimeInMillis(timestamp);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    long serviceDate = c.getTimeInMillis();

    TrainInstance instance = new TrainInstance(trainId, serviceDate);

    fillTimepointsForTrainInstance(instance);

    _trainsById.put(trainId, instance);
    _narrativeService.addMessage(instance,
        "activation: trainUid=" + body.getTrainUid());
    _narrativeService.addMessage(instance.getTrainReportingNumber(), timestamp,
        "activation: trainUid=" + body.getTrainUid() + " trainId=" + trainId);
  }

  private void fillTimepointsForTrainInstance(TrainInstance instance) {
    BasicScheduleElement schedule = instance.getSchedule();
    for (TimepointElement timepoint : schedule.getTimepoints()) {
      TiplocInsertElement insert = _insertsByTiploc.get(timepoint.getTiploc());
      if (insert == null) {
        continue;
      }
      instance.putTimepoint(insert.getStanox(), timepoint);
    }
  }

  private void handleCancellation(TrainMovementMessage message) {
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance == null) {
      _statisticsService.incrementUnknownCancelledTrainIdCount();
      return;
    } else {
      /**
       * We don't want to remove the train instance, since it might be
       * reinstated in the future. But we do clear any stop time updates it
       * might have.
       */
      instance.setStopTimeUpdate(null);

      _narrativeService.addMessage(instance, "cancelation");
    }
  }

  private void handleMovement(TrainMovementMessage message) {
    TrainMovementBody body = message.getBody();
    _narrativeService.addMessage(
        body.getTrainId().substring(2, 6),
        Long.parseLong(message.getHeader().getMsgQueueTimestamp()),
        "movement: trainId=" + body.getTrainId() + " stanox="
            + body.getLocStanox());
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance == null) {
      _statisticsService.incrementUnknownTrainIdCount();
      return;
    }

    Set<TrainInstance> instances = _trainsByShortId.get(instance.getTrainReportingNumber());
    if (instances == null) {
      instances = new HashSet<TrainInstance>();
      _trainsByShortId.put(instance.getTrainReportingNumber(), instances);
    }
    instances.add(instance);

    if (body.getLocStanox() == null) {
      _statisticsService.incrementEmptyLocStanoxCount();
    }
    int stanox = Integer.parseInt(body.getLocStanox());
    TimepointElement timepoint = null; // what?
    if (timepoint == null) {
      _statisticsService.incrementUnknownStanoxCount();
      return;
    }

    if (body.getPlatform() != null) {
      String platform = body.getPlatform().trim();
      if (!platform.isEmpty() && !platform.equals(timepoint.getPlatform())) {
        _statisticsService.incrementPlatformChange();
      }
    }

    _narrativeService.addMessage(instance, "movement: stanox=" + stanox
        + " tiploc=" + timepoint.getTiploc());

    if (body.getActualTimestamp().isEmpty()
        || body.getPlannedTimestamp().isEmpty()) {
      return;
    }

    long actualTimestamp = Long.parseLong(body.getActualTimestamp());
    long plannedTimestamp = Long.parseLong(body.getPlannedTimestamp());
    int delay = (int) ((actualTimestamp - plannedTimestamp) / 1000);

    StopTimeEvent.Builder departure = StopTimeEvent.newBuilder();
    departure.setDelay(delay);

    StopTimeUpdate.Builder stopTimeUpdate = StopTimeUpdate.newBuilder();
    stopTimeUpdate.setStopId(timepoint.getTiploc());
    stopTimeUpdate.setDeparture(departure);

    instance.setStopTimeUpdate(stopTimeUpdate.build());
  }

  private void handleUnidentifiedTrain(TrainMovementMessage message) {

  }

  private void handleReinstatement(TrainMovementMessage message) {
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance == null) {
      _statisticsService.incrementUnknownReinstatedTrainIdCount();
      return;
    } else {
      _narrativeService.addMessage(instance, "reinstatement");
    }
  }

  private void handleChangeOfOrigin(TrainMovementMessage message) {
    /**
     * I'm not sure what this means in practice.
     */
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance != null) {
      _narrativeService.addMessage(instance, "change-of-origin");
    }
  }

  private void handleChangleOfIdentity(TrainMovementMessage message) {
    /**
     * Theoretically, we should
     */
    TrainMovementBody body = message.getBody();
    String trainId = body.getTrainId();
    String revisedTrainId = body.getRevisedTrainId();
    if (!trainId.equals(revisedTrainId)) {
      TrainInstance train = _trainsById.remove(trainId);
      if (train != null) {
        throw new UnsupportedOperationException();
        
        /*
        train = new TrainInstance(revisedTrainId, train);
        _trainsById.put(revisedTrainId, train);
        */
      }
    }
  }

  private void handleChangeOfLocation(TrainMovementMessage message) {
    /**
     * I haven't seen one of these in live data yet.
     */
  }

  private void processTrainDescriberMessage(TrainDescriberMessage message) {
    if (message.getStep() != null) {
      processBerthStepMessage(message.getStep());
    }
    if (message.getInterpose() != null) {
      processBerthInterposeMessage(message.getInterpose());
    }
    if (message.getCancel() != null) {
      processBerthCanelMessage(message.getCancel());
    }
    if (message.getHeartbeat() != null) {
      processBerthHeartbeatMessage(message.getHeartbeat());
    }
  }

  private Set<String> _berthIds = new HashSet<String>();

  private void processBerthStepMessage(BerthStepMessage step) {
    if (_berthIds.add(step.getAreaId() + "_" + step.getFrom())
        || _berthIds.add(step.getAreaId() + "_" + step.getTo())) {
      if (_berthIds.size() % 1000 == 0) {
        _log.info("berth ids=" + _berthIds.size());
      }
    }
    _narrativeService.addMessage(step, "berth step: areaId=" + step.getAreaId()
        + " from=" + step.getFrom() + " to=" + step.getTo());
    TrainInstance instance = getTrainInstanceForShortTrainId(step,
        step.getFrom(), step.getTo());
    if (instance == null) {
      return;
    }
    _narrativeService.addMessage(instance,
        "berth step: areaId=" + step.getAreaId() + " from=" + step.getFrom()
            + " to=" + step.getTo());
  }

  private void processBerthCanelMessage(BerthCancelMessage cancel) {
    _narrativeService.addMessage(
        cancel,
        "berth cancel: areaId=" + cancel.getAreaId() + " from="
            + cancel.getFrom());
    TrainInstance instance = getTrainInstanceForShortTrainId(cancel,
        cancel.getFrom(), null);
    if (instance == null) {
      return;
    }
    _narrativeService.addMessage(
        instance,
        "berth cancel: areaId=" + cancel.getAreaId() + " from="
            + cancel.getFrom());
  }

  private void processBerthInterposeMessage(BerthInterposeMessage interpose) {
    _narrativeService.addMessage(interpose, "berth interpose: areaId="
        + interpose.getAreaId() + " to=" + interpose.getTo());

    TrainInstance instance = getTrainInstanceForShortTrainId(interpose, null,
        interpose.getTo());
    if (instance == null) {
      return;
    }
    _narrativeService.addMessage(instance, "berth interpose: areaId="
        + interpose.getAreaId() + " to=" + interpose.getTo());
  }

  private void processBerthHeartbeatMessage(BerthHeartbeatMessage heartbeat) {
    _narrativeService.addMessage(heartbeat, "berth heartbeat: areaId="
        + heartbeat.getAreaId());
  }

  private BasicScheduleElement getBestScheduleForTrainUid(String trainUid) {
    BasicScheduleElement schedule = _schedulesByTrainUid.get(trainUid);
    if (schedule == null) {
      _statisticsService.incrementUnknownTrainUidCount();
    }
    return schedule;
  }

  private TrainInstance getTrainInstanceForTrainId(TrainMovementMessage message) {
    TrainMovementBody body = message.getBody();
    String trainId = body.getTrainId();
    if (trainId == null) {
      return null;
    }
    TrainInstance instance = _trainsById.get(trainId);
    if (instance != null) {
      instance.setLastUpdateTime(Long.parseLong(message.getHeader().getMsgQueueTimestamp()));
    }
    return instance;
  }

  private long _noMatches = 0;

  private long _multipleMatches = 0;

  private long _singleMatch = 0;

  private TrainInstance getTrainInstanceForShortTrainId(BerthMessage message,
      String fromBerthId, String toBerthId) {
    String trainReportingNumber = message.getDescr();
    Set<TrainInstance> instances = _trainsByShortId.get(trainReportingNumber);
    if (instances == null || instances.isEmpty()) {
      _noMatches++;
      return null;
    } else if (instances.size() == 1) {
      _singleMatch++;
      return instances.iterator().next();
    }

    Set<TrainInstance> matches = new HashSet<TrainInstance>();

    if (fromBerthId != null) {
      matchBerthId(message, fromBerthId, instances, matches);
    }
    if (toBerthId != null) {
      matchBerthId(message, toBerthId, instances, matches);
    }

    if (matches.isEmpty()) {
      _noMatches++;
      // _log.warn("no matches");
    } else if (matches.size() > 1) {
      _multipleMatches++;
      // _log.warn("multiple matches");
    }
    _singleMatch++;
    TrainInstance instance = instances.iterator().next();
    instance.setLastUpdateTime(Long.parseLong(message.getTime()));
    // System.out.println(_noMatches + " " + _singleMatch + " " +
    // _multipleMatches);
    return instance;
  }

  private void matchBerthId(BerthMessage step, String berthId,
      Set<TrainInstance> instances, Set<TrainInstance> matches) {
    Integer stanox = _stanoxByBerthId.get(step.getAreaId() + "_" + berthId);
    if (stanox != null) {
      String prefix = Integer.toString(stanox).substring(0, 2);
      for (TrainInstance instance : instances) {
        if (true) {
          throw new UnsupportedOperationException();
        }
        if (false /*instance.hasMatchingStanoxPrefix(prefix)*/) {
          matches.add(instance);
        }
      }
    }
  }

  private void removeExpiredTrains() {
    Iterator<TrainInstance> it = _trainsById.values().iterator();
    while (it.hasNext()) {
      TrainInstance instance = it.next();
      if (true) {
        throw new UnsupportedOperationException();
      }
      int timeout = false /*instance.hasMoved()*/ ? _trainMovemenExpirationTimeInSeconds
          : _trainActivationExpirationTimeInSeconds;
      if (_mostRecentTimestamp > instance.getLastUpdateTime() + timeout * 1000) {
        it.remove();
        _narrativeService.closeMessages(instance);
        Set<TrainInstance> instances = _trainsByShortId.get(instance.getTrainReportingNumber());
        if (instances != null) {
          instances.remove(instance);
          if (instances.isEmpty()) {
            _trainsByShortId.remove(instance.getTrainReportingNumber());
          }
        }
      }
    }
  }

  private void updateFeeds() {
    if (_inReplay) {
      return;
    }

    FeedMessage.Builder tripUpdatesFeed = GtfsRealtimeLibrary.createFeedMessageBuilder();

    for (TrainInstance instance : _trainsById.values()) {

      BasicScheduleElement schedule = instance.getSchedule();

      TripDescriptor.Builder tripDescriptor = TripDescriptor.newBuilder();
      tripDescriptor.setTripId(schedule.getTrainUid());

      VehicleDescriptor.Builder vehicleDescriptor = VehicleDescriptor.newBuilder();
      vehicleDescriptor.setId(instance.getTrainId());

      if (instance.getStopTimeUpdate() != null) {
        TripUpdate.Builder tripUpdate = TripUpdate.newBuilder();
        tripUpdate.setTrip(tripDescriptor);
        tripUpdate.setVehicle(vehicleDescriptor);
        tripUpdate.addStopTimeUpdate(instance.getStopTimeUpdate());

        FeedEntity.Builder tripUpdateEntity = FeedEntity.newBuilder();
        tripUpdateEntity.setTripUpdate(tripUpdate);
        tripUpdateEntity.setId(instance.getTrainId());
        tripUpdatesFeed.addEntity(tripUpdateEntity);

      }
    }

    _gtfsRealtimeProvider.setTripUpdates(tripUpdatesFeed.build(), false);
    _gtfsRealtimeProvider.fireUpdate();
  }

  private void saveState() throws IOException {
    if (_statePath == null || _inReplay) {
      return;
    }
    List<TrainState> states = new ArrayList<TrainState>();
    for (TrainInstance instance : _trainsById.values()) {
      TrainState state = new TrainState();
      state.setTrainId(instance.getTrainId());
      state.setTrainUid(instance.getSchedule().getTrainUid());
      state.setServiceDate(instance.getServiceDate());
      state.setLastUpdateTimestamp(instance.getLastUpdateTime());
      states.add(state);
    }

    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(
        new FileOutputStream(_statePath)));
    out.writeObject(states);
    out.close();
  }

  @SuppressWarnings("unchecked")
  private void reloadState() throws IOException {
    if (_statePath == null || !_statePath.exists()) {
      return;
    }
    _log.info("reloading state");
    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(
        new FileInputStream(_statePath)));

    try {
      List<TrainState> states = (List<TrainState>) in.readObject();
      for (TrainState state : states) {
        BasicScheduleElement schedule = _schedulesByTrainUid.get(state.getTrainUid());
        if (schedule == null) {
          _log.warn("unknown schedule referenced in saved state: "
              + state.getTrainUid());
          continue;
        }
        TrainInstance instance = new TrainInstance(state.getTrainId(),
            state.getServiceDate());
        fillTimepointsForTrainInstance(instance);
        _trainsById.put(state.getTrainId(), instance);
      }
      in.close();
    } catch (Exception ex) {
      throw new IllegalStateException("error loading state from " + _statePath,
          ex);
    }
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
        TiplocInsertElement existing = _insertsByTiploc.put(insert.getTiploc(),
            insert);
        if (existing != null) {
          _log.warn("duplicate insert tiploc=" + insert.getTiploc());
        }
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
