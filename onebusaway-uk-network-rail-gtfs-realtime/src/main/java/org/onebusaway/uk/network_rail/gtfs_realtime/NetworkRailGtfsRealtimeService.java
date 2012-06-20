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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.onebusaway.uk.atoc.timetable_parser.StationElement;
import org.onebusaway.uk.atoc.timetable_parser.TimetableBundle;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.TimepointElement;
import org.onebusaway.uk.network_rail.cif.TiplocInsertElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.Body;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.Message;
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
public class NetworkRailGtfsRealtimeService {

  private static final Logger _log = LoggerFactory.getLogger(NetworkRailGtfsRealtimeService.class);

  private Gson _gson;

  private GtfsRealtimeMutableProvider _gtfsRealtimeProvider;

  private LoggingService _loggingService;

  private Map<String, StationElement> _stationsByTiploc = new HashMap<String, StationElement>();

  private Map<String, TiplocInsertElement> _insertsByTiploc = new HashMap<String, TiplocInsertElement>();

  private Map<String, BasicScheduleElement> _schedulesByTrainUid = new HashMap<String, BasicScheduleElement>();

  private Map<String, TrainInstance> _trainsById = new HashMap<String, TrainInstance>();

  private long _mostRecentTimestamp = 0;

  private Statistics _statistics = new Statistics();

  private File _atocTimetablePath;

  private File _naptanPath;

  private File _statePath;

  private boolean _inReplay = false;

  /**
   * If it's been more than the specified number of seconds since we've heard an
   * update from a train, we remove it from the list of currently tracked
   * trains.
   */
  private int _trainExpirationTimeInSeconds = 60 * 60;

  public NetworkRailGtfsRealtimeService() {
    _gson = new GsonBuilder().setFieldNamingPolicy(
        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  }

  @Inject
  public void setGtfsRealtimeProvider(GtfsRealtimeMutableProvider provider) {
    _gtfsRealtimeProvider = provider;
  }

  @Inject
  public void setLoggingService(LoggingService loggingService) {
    _loggingService = loggingService;
  }

  public void setAtocTimetablePath(File atocTimetablePath) {
    _atocTimetablePath = atocTimetablePath;
  }

  public void setNaptanPath(File naptanPath) {
    _naptanPath = naptanPath;
  }

  public void setStatePath(File statePath) {
    _statePath = statePath;
  }

  public void setTrainExpirationTimeInSeconds(int timeInSeconds) {
    _trainExpirationTimeInSeconds = timeInSeconds;
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

    if (_statePath != null) {
      reloadState();
    }

    _inReplay = true;
    _loggingService.replayLogs();
    _inReplay = false;
    updateFeeds();
  }

  public void processMessages(String jsonMessage) throws IOException {
    if (!jsonMessage.startsWith("[")) {
      return;
    }
    if (!_inReplay) {
      _loggingService.logMessage(jsonMessage);
    }
    Message[] messages = _gson.fromJson(jsonMessage, Message[].class);
    for (Message message : messages) {
      processMessage(message);
    }
    removeExpiredTrains();
    updateFeeds();
    saveState();
  }

  private void processMessage(Message message) {

    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));

    String trainId = message.getBody().getTrainId();
    _statistics.incrementMessage(msgType, trainId);

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

  private void handleActivation(Message message) {
    Body body = message.getBody();
    BasicScheduleElement schedule = getBestScheduleForTrainUid(body.getTrainUid());
    if (schedule == null) {
      return;
    }
    String trainId = body.getTrainId();
    TrainInstance instance = new TrainInstance(trainId, schedule,
        Long.parseLong(message.getHeader().getMsgQueueTimestamp()));

    fillTimepointsForTrainInstance(instance);

    _trainsById.put(trainId, instance);
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

  private void handleCancellation(Message message) {
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance == null) {
      _statistics.incrementUnknownCancelledTrainIdCount();
      return;
    } else {
      /**
       * We don't want to remove the train instance, since it might be
       * reinstated in the future. But we do clear any stop time updates it
       * might have.
       */
      instance.setStopTimeUpdate(null);
    }
  }

  private void handleMovement(Message message) {
    Body body = message.getBody();
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance == null) {
      _statistics.incrementUnknownTrainIdCount();
      return;
    }
    if (body.getLocStanox() == null) {
      _statistics.incrementEmptyLocStanoxCount();
    }
    int stanox = Integer.parseInt(body.getLocStanox());
    TimepointElement timepoint = instance.getTimepointForStanox(stanox);
    if (timepoint == null) {
      _statistics.incrementUnknownStanoxCount();
      return;
    }

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

  private void handleUnidentifiedTrain(Message message) {

  }

  private void handleReinstatement(Message message) {
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance == null) {
      _statistics.incrementUnknownReinstatedTrainIdCount();
      return;
    } else {

    }
  }

  private void handleChangeOfOrigin(Message message) {
    /**
     * I'm not sure what this means in practice.
     */
    TrainInstance instance = getTrainInstanceForTrainId(message);
    if (instance != null) {

    }
  }

  private void handleChangleOfIdentity(Message message) {
    /**
     * Theoretically, we should
     */
    Body body = message.getBody();
    String trainId = body.getTrainId();
    String revisedTrainId = body.getRevisedTrainId();
    if (!trainId.equals(revisedTrainId)) {
      TrainInstance train = _trainsById.remove(trainId);
      if (train != null) {
        train = new TrainInstance(revisedTrainId, train);
        _trainsById.put(revisedTrainId, train);
      }
    }
  }

  private void handleChangeOfLocation(Message message) {
    /**
     * I haven't seen one of these in live data yet.
     */
  }

  private BasicScheduleElement getBestScheduleForTrainUid(String trainUid) {
    BasicScheduleElement schedule = _schedulesByTrainUid.get(trainUid);
    if (schedule == null) {
      _statistics.incrementUnknownTrainUidCount();
    }
    return schedule;
  }

  private TrainInstance getTrainInstanceForTrainId(Message message) {
    Body body = message.getBody();
    String trainId = body.getTrainId();
    if (trainId == null) {
      return null;
    }
    TrainInstance instance = _trainsById.get(message);
    if (instance != null) {
      instance.setLastUpdateTime(Long.parseLong(message.getHeader().getMsgQueueTimestamp()));
    }
    return instance;
  }

  private void removeExpiredTrains() {
    Iterator<TrainInstance> it = _trainsById.values().iterator();
    while (it.hasNext()) {
      TrainInstance instance = it.next();
      if (_mostRecentTimestamp > instance.getLastUpdateTime()
          + _trainExpirationTimeInSeconds * 1000) {
        it.remove();
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
            schedule, state.getLastUpdateTimestamp());
        fillTimepointsForTrainInstance(instance);
        _trainsById.put(state.getTrainId(), instance);
      }
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
