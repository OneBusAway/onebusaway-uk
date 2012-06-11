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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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

  private Map<String, StationElement> _stationsByTiploc = new HashMap<String, StationElement>();

  private Map<String, TiplocInsertElement> _insertsByTiploc = new HashMap<String, TiplocInsertElement>();

  private Map<String, BasicScheduleElement> _schedulesByTrainUid = new HashMap<String, BasicScheduleElement>();

  private Map<String, TrainInstance> _trainsById = new HashMap<String, NetworkRailGtfsRealtimeService.TrainInstance>();

  private Map<String, ETrainMovementMessageType> _prevState = new HashMap<String, ETrainMovementMessageType>();

  private SortedMap<String, Long> _stateTranistionCounts = new TreeMap<String, Long>();

  private File _atocTimetablePath;

  private File _naptanPath;

  private long _messageCount;

  private long _unknownTrainUidCount;

  private long _unknownTrainIdCount;

  private long _unknownCancelledTrainIdCount;

  private long _unknownReinstatedTrainIdCount;

  private long _emptyLocStanoxCount;

  private long _unknownStanoxCount;

  private long _trainActivationCount;

  private long _trainCancellationCount;

  private long _trainMovementCount;

  private long _unidentifiedTrainCount;

  private long _trainReinstatementCount;

  private long _trainChangeOfOriginCount;

  private long _trainChangeOfIdentityCount;

  private long _trainChangeOfLocationCount;

  public NetworkRailGtfsRealtimeService() {
    _gson = new GsonBuilder().setFieldNamingPolicy(
        FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
  }

  @Inject
  public void setGtfsRealtimeProvider(GtfsRealtimeMutableProvider provider) {
    _gtfsRealtimeProvider = provider;
  }

  public void setAtocTimetablePath(File atocTimetablePath) {
    _atocTimetablePath = atocTimetablePath;
  }

  public void setNaptanPath(File naptanPath) {
    _naptanPath = naptanPath;
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
  }

  public void processMessages(String jsonMessage) {
    if (jsonMessage.isEmpty()) {
      return;
    }
    Message[] messages = _gson.fromJson(jsonMessage, Message[].class);
    for (Message message : messages) {
      processMessage(message);
    }
    updateFeeds();
  }

  private void processMessage(Message message) {
    _messageCount++;
    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));

    if (isLogged(message)) {
      _log.info("logged");
    }

    switch (msgType) {
      case ACTIVATION:
        _trainActivationCount++;
        handleActivation(message);
        break;
      case CANCELLATION:
        _trainCancellationCount++;
        handleCancellation(message);
        break;
      case MOVEMENT:
        _trainMovementCount++;
        handleMovement(message);
        break;
      case UNIDENTIFIED_TRAIN:
        _unidentifiedTrainCount++;
        handleUnidentifiedTrain(message);
        break;
      case REINSTATEMENT:
        _trainReinstatementCount++;
        handleReinstatement(message);
        break;
      case CHANGE_OF_ORIGIN:
        _trainChangeOfOriginCount++;
        handleChangeOfOrigin(message);
        break;
      case CHANGE_OF_IDENTITY:
        _trainChangeOfIdentityCount++;
        handleChangleOfIdentity(message);
        break;
      case CHANGE_OF_LOCATION:
        _trainChangeOfLocationCount++;
        handleChangeOfLocation(message);
        break;

    }

    String trainId = message.getBody().getTrainId();
    if (trainId != null && !trainId.isEmpty()) {
      ETrainMovementMessageType prev = _prevState.put(trainId, msgType);
      if (prev != null) {
        String key = prev + " => " + msgType;
        Long count = _stateTranistionCounts.get(key);
        count = count == null ? 1 : count + 1;
        _stateTranistionCounts.put(key, count);
      }
    }

    if (_messageCount % 1000 == 0) {
      printStats();
    }
  }

  private void handleActivation(Message message) {
    Body body = message.getBody();
    BasicScheduleElement schedule = getBestScheduleForTrainUid(body.getTrainUid());
    if (schedule == null) {
      return;
    }
    String trainId = body.getTrainId();
    TrainInstance instance = new TrainInstance(trainId, schedule);

    for (TimepointElement timepoint : schedule.getTimepoints()) {
      TiplocInsertElement insert = _insertsByTiploc.get(timepoint.getTiploc());
      if (insert == null) {
        continue;
      }
      instance.putTimepoint(insert.getStanox(), timepoint);
    }

    _trainsById.put(trainId, instance);
  }

  private void handleCancellation(Message message) {
    Body body = message.getBody();
    String trainId = body.getTrainId();
    TrainInstance instance = _trainsById.get(trainId);
    if (instance == null) {
      _unknownCancelledTrainIdCount++;
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
    String trainId = body.getTrainId();
    TrainInstance instance = _trainsById.get(trainId);
    if (instance == null) {
      _unknownTrainIdCount++;
      return;
    }
    if (body.getLocStanox() == null) {
      _emptyLocStanoxCount++;
    }
    int stanox = Integer.parseInt(body.getLocStanox());
    TimepointElement timepoint = instance.getTimepointForStanox(stanox);
    if (timepoint == null) {
      _unknownStanoxCount++;
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
    Body body = message.getBody();
    String trainId = body.getTrainId();
    TrainInstance instance = _trainsById.get(trainId);
    if (instance == null) {
      _unknownReinstatedTrainIdCount++;
      return;
    } else {
      System.out.println("here");
    }
  }

  private void handleChangeOfOrigin(Message message) {
    /**
     * I'm not sure what this means in practice.
     */
    Body body = message.getBody();
    String trainId = body.getTrainId();
    TrainInstance instance = _trainsById.get(trainId);
    if (instance != null) {
      System.out.println("here");
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
      _unknownTrainUidCount++;
    }
    return schedule;
  }

  private void updateFeeds() {

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

  private boolean isLogged(Message message) {
    String trainId = message.getBody().getTrainId();
    return trainId != null && trainId.equals("what");

  }

  private void printStats() {
    System.out.println("========================================");
    System.out.println("                  messages=" + _messageCount);
    System.out.println("           unknownTrainUid=" + _unknownTrainUidCount);
    System.out.println("            unknownTrainId=" + _unknownTrainIdCount);
    System.out.println("   unknownCancelledTrainId="
        + _unknownCancelledTrainIdCount);
    System.out.println("  unknownReinstatedTrainId="
        + _unknownReinstatedTrainIdCount);
    System.out.println("            emptyLocStanox=" + _emptyLocStanoxCount);
    System.out.println("             unknownStanox=" + _unknownStanoxCount);
    System.out.println("      trainActivationCount=" + _trainActivationCount);
    System.out.println("    trainCancellationCount=" + _trainCancellationCount);
    System.out.println("        trainMovementCount=" + _trainMovementCount);
    System.out.println("    unidentifiedTrainCount=" + _unidentifiedTrainCount);
    System.out.println("   trainReinstatementCount=" + _trainReinstatementCount);
    System.out.println("  trainChangeOfOriginCount="
        + _trainChangeOfOriginCount);
    System.out.println("trainChangeOfIdentityCount="
        + _trainChangeOfIdentityCount);
    System.out.println("trainChangeOfLocationCount="
        + _trainChangeOfLocationCount);

    for (Map.Entry<String, Long> entry : _stateTranistionCounts.entrySet()) {
      System.out.println(entry.getKey() + " = " + entry.getValue());
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

  private class TrainInstance {

    private final String trainId;

    private final BasicScheduleElement schedule;

    private final Map<Integer, TimepointElement> timepointsByStanox = new HashMap<Integer, TimepointElement>();

    private StopTimeUpdate _stopTimeUpdate;

    public TrainInstance(String trainId, BasicScheduleElement schedule) {
      this.trainId = trainId;
      this.schedule = schedule;
    }

    public TrainInstance(String trainId, TrainInstance train) {
      this(trainId, train.schedule);
      timepointsByStanox.putAll(train.timepointsByStanox);
    }

    public String getTrainId() {
      return trainId;
    }

    public BasicScheduleElement getSchedule() {
      return schedule;
    }

    public void putTimepoint(int stanox, TimepointElement timepoint) {
      timepointsByStanox.put(stanox, timepoint);
    }

    public TimepointElement getTimepointForStanox(int stanox) {
      return timepointsByStanox.get(stanox);
    }

    public void setStopTimeUpdate(StopTimeUpdate stopTimeUpdate) {
      _stopTimeUpdate = stopTimeUpdate;
    }

    public StopTimeUpdate getStopTimeUpdate() {
      return _stopTimeUpdate;
    }
  }
}
