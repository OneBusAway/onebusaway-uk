package org.onebusaway.uk.network_rail.gtfs_realtime.instance;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.onebusaway.status_exporter.StatusProviderService;
import org.onebusaway.uk.network_rail.cif.BasicScheduleElement;
import org.onebusaway.uk.network_rail.cif.ETrainClass;
import org.onebusaway.uk.network_rail.cif.TimepointElement;
import org.onebusaway.uk.network_rail.gtfs_realtime.ETrainMovementMessageType;
import org.onebusaway.uk.network_rail.gtfs_realtime.StatisticsService;
import org.onebusaway.uk.network_rail.gtfs_realtime.TimetableService;
import org.onebusaway.uk.network_rail.gtfs_realtime.TrainDescriberHandler;
import org.onebusaway.uk.network_rail.gtfs_realtime.TrainMovementHandler;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthCancelMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthInterposeMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.BerthStepMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.SerializedNarrative;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainDescriberMessage;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainMovementBody;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.TrainMovementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class TrainTrackingService implements TrainMovementHandler,
    TrainDescriberHandler, StatusProviderService {

  private static final int TIMESTAMP_DRIFT_THRESHOLD = 5 * 60 * 1000;

  private Logger _log = LoggerFactory.getLogger(TrainTrackingService.class);

  private DateFormat _format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

  private Map<String, TrainReportingNumberInstance> _trainReportingNumberInstances = new HashMap<String, TrainReportingNumberInstance>();

  private TimetableService _timetableService;

  private StatisticsService _statisticsService;

  private List<TrainTrackingListener> _listeners = new ArrayList<TrainTrackingListener>();

  private long _timepointMatchThreshold = 30 * 60 * 1000;

  private long _lastPruneTimestamp = 0;

  private long _lastUpdateTime = 0;

  private long _pruneFrequency = 30 * 60 * 1000;

  private long _pruneTimeout = 2 * 60 * 60 * 1000;

  @Inject
  public void setTimetableService(TimetableService timetableService) {
    _timetableService = timetableService;
  }

  @Inject
  public void setStatisticsService(StatisticsService statisticsService) {
    _statisticsService = statisticsService;
  }

  public void addListener(TrainTrackingListener listener) {
    _listeners.add(listener);
  }

  public void addTimepointMatchThreshold(long timepointMatchThreshold) {
    _timepointMatchThreshold = timepointMatchThreshold;
  }

  public void pruneAllInstances() {
    pruneStaleInstances(true);
  }

  @Override
  public synchronized void handleTrainMovementMessage(long fileTimestamp,
      TrainMovementMessage message, String source) {
    long msgTimestamp = Long.parseLong(message.getHeader().getMsgQueueTimestamp());
    long timestamp = pickBestTimestamp(msgTimestamp, fileTimestamp);
    TrainMovementBody body = message.getBody();
    String trainId = body.getTrainId();
    String trainReportingNumber = trainId.substring(2, 6);
    int trainClassCode = Integer.parseInt(trainReportingNumber.substring(0, 1));
    ETrainClass trainClass = ETrainClass.getTrainClassForCode(trainClassCode);
    if (!trainClass.isPassengerClass()) {
      return;
    }
    TrainReportingNumberInstance instance = getTrainReportingNumberInstance(
        trainReportingNumber, timestamp);
    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));

    SerializedNarrative.Event event = buildTrainMovementEvent(timestamp,
        source, message);

    instance.addEvent(event);
    instance.setLastUpdateTime(timestamp);

    TrainInstance trainInstance = instance.getTrainInstanceForTrainId(trainId);

    trainInstance.addEvent(event);
    trainInstance.setLastUpdateTime(timestamp);

    _statisticsService.incrementMessage(msgType, trainId);

    switch (msgType) {
      case ACTIVATION:
        handleActivation(trainInstance, body);
        break;
      case CANCELLATION:
        handleCancellation(trainInstance, body);
      case MOVEMENT:
        handleMovement(trainInstance, body, timestamp);
        break;
      default:
        break;
    }

    checkForPrune();
  }

  @Override
  public synchronized void handleTrainDescriberMessage(long fileTimestamp,
      TrainDescriberMessage message, String source) {
    if (message.getStep() != null) {
      processBerthStepMessage(fileTimestamp, message.getStep(), source);
    }
    if (message.getCancel() != null) {
      processBerthCancelMessage(fileTimestamp, message.getCancel(), source);
    }
    if (message.getInterpose() != null) {
      processBerthInterposeMessage(fileTimestamp, message.getInterpose(),
          source);
    }
    checkForPrune();
  }

  @Override
  public synchronized void getStatus(Map<String, String> status) {
    String prefix = "network_rail_gtfs_realtime.train_tracker.";
    status.put(prefix + "trainReportingInstanceCount",
        Integer.toString(_trainReportingNumberInstances.size()));
    int totalTrains = 0;
    int activatedTrains = 0;
    for (TrainReportingNumberInstance instance : _trainReportingNumberInstances.values()) {
      for (TrainInstance trainInstance : instance.getAllTrainInstances()) {
        totalTrains++;
        if (trainInstance.getSchedule() != null) {
          activatedTrains++;
        }
      }
    }
    status.put(prefix + "totalTrainCount", Integer.toString(totalTrains));
    status.put(prefix + "totalActivatedTrainCount",
        Integer.toString(activatedTrains));
  }

  /****
   * Private Methods
   ****/

  private long pickBestTimestamp(long msgTimestamp, long fileTimestamp) {
    if (Math.abs(msgTimestamp - fileTimestamp) < TIMESTAMP_DRIFT_THRESHOLD) {
      return msgTimestamp;
    }
    _log.info("timestamp drift: msgTimestamp="
        + _format.format(new Date(msgTimestamp)) + " vs fileTimestamp="
        + _format.format(new Date(fileTimestamp)));

    Calendar c = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
    c.setTimeInMillis(msgTimestamp);
    int hours = (int) Math.round((fileTimestamp - msgTimestamp)
        / (60 * 60 * 1000.0));
    c.add(Calendar.HOUR_OF_DAY, hours);
    return Math.abs(c.getTimeInMillis() - fileTimestamp) < TIMESTAMP_DRIFT_THRESHOLD
        ? c.getTimeInMillis() : fileTimestamp;
  }

  private SerializedNarrative.Event buildTrainMovementEvent(long timestamp,
      String source, TrainMovementMessage message) {
    SerializedNarrative.Event.Builder eventBuilder = SerializedNarrative.Event.newBuilder();
    eventBuilder.setTimestamp(timestamp);
    eventBuilder.setSource(source);
    SerializedNarrative.TrainMovementEvent.Builder trainMovementEvent = eventBuilder.getTrainMovementBuilder();

    ETrainMovementMessageType msgType = ETrainMovementMessageType.getTypeForCode(Integer.parseInt(message.getHeader().getMsgType()));
    trainMovementEvent.setType(SerializedNarrative.TrainMovementEvent.Type.valueOf(msgType.name()));

    TrainMovementBody body = message.getBody();
    if (body.getTrainId() != null) {
      trainMovementEvent.setTrainId(body.getTrainId());
    }
    if (body.getTrainUid() != null) {
      trainMovementEvent.setTrainUid(body.getTrainUid());
    }
    if (body.getLocStanox() != null) {
      int stanox = Integer.parseInt(body.getLocStanox());
      if (stanox != 0) {
        trainMovementEvent.setStanoxId(stanox);
      }
    }
    if (body.getEventSource() != null) {
      trainMovementEvent.setEventSource(body.getEventSource());
    }
    SerializedNarrative.Event event = eventBuilder.build();
    return event;
  }

  private void handleActivation(TrainInstance trainInstance,
      TrainMovementBody body) {
    BasicScheduleElement schedule = _timetableService.getBestScheduleForTrainUid(body.getTrainUid());
    trainInstance.setSchedule(schedule);
    if (schedule != null) {
      for (TimepointElement timepoint : schedule.getTimepoints()) {
        int stanox = _timetableService.getStanoxForTiploc(timepoint.getTiploc());
        if (stanox != 0) {
          trainInstance.putTimepoint(stanox, timepoint);
        }
      }
    }
  }

  private void handleCancellation(TrainInstance instance,
      TrainMovementBody trainMovementBody) {
    if (instance.getSchedule() == null) {
      _statisticsService.incrementUnknownCancelledTrainIdCount();
    }

    /**
     * We don't want to remove the train instance, since it might be reinstated
     * in the future. But we do clear any stop time updates it might have.
     */
    instance.setStopTimeUpdate(null);
  }

  private void handleMovement(TrainInstance trainInstance,
      TrainMovementBody body, long time) {

    if (trainInstance.getSchedule() == null) {
      _statisticsService.incrementUnknownTrainIdCount();
    }

    if (body.getLocStanox() == null) {
      _statisticsService.incrementEmptyLocStanoxCount();
      return;
    }
    int stanox = Integer.parseInt(body.getLocStanox());
    trainInstance.setLastStanox(time, stanox);

    List<TimepointElement> timepoints = trainInstance.getTimepointForStanox(stanox);
    TimepointElement timepoint = null;
    if (!timepoints.isEmpty()) {
      _statisticsService.incrementUnknownStanoxCount();
      timepoint = getBestTimepointForTrainMovement(timepoints, body);
    }

    if (body.getActualTimestamp() != null
        && !body.getActualTimestamp().isEmpty()
        && body.getPlannedTimestamp() != null
        && !body.getPlannedTimestamp().isEmpty()) {
      long actualTimestamp = Long.parseLong(body.getActualTimestamp());
      long plannedTimestamp = Long.parseLong(body.getPlannedTimestamp());
      int scheduleDeviation = (int) ((actualTimestamp - plannedTimestamp) / 1000);
      trainInstance.setScheduleDeviation(scheduleDeviation);
    } else if (timepoint != null) {
      long expectedTime = getTimeForTimepoint(trainInstance, timepoint);
      int scheduleDeviation = (int) ((time - expectedTime) / 1000);
      trainInstance.setScheduleDeviation(scheduleDeviation);
    }

    if (body.getPlatform() != null && timepoint != null) {
      String platform = body.getPlatform().trim();
      if (!platform.isEmpty() && !platform.equals(timepoint.getPlatform())) {
        _statisticsService.incrementPlatformChange();
      }
    }
  }

  private TimepointElement getBestTimepointForTrainMovement(
      List<TimepointElement> timepoints, TrainMovementBody body) {
    if (timepoints.size() == 1) {
      return timepoints.get(0);
    }

    return null;
  }

  private void processBerthStepMessage(long fileTimestamp,
      BerthStepMessage step, String source) {
    long timestamp = pickBestTimestamp(step.getTimeAsLong(), fileTimestamp);
    TrainReportingNumberInstance instance = getTrainReportingNumberInstance(
        step.getDescr(), timestamp);
    instance.setLastUpdateTime(timestamp);

    String berthAreaId = step.getAreaId();

    SerializedNarrative.Event.Builder eventBuilder = SerializedNarrative.Event.newBuilder();
    eventBuilder.setTimestamp(timestamp);
    eventBuilder.setSource(source);
    SerializedNarrative.BerthStepEvent.Builder berthStepEvent = eventBuilder.getBerthStepBuilder();
    berthStepEvent.setFromBerthId(berthAreaId + "_" + step.getFrom());
    berthStepEvent.setToBerthId(berthAreaId + "_" + step.getTo());
    SerializedNarrative.Event event = eventBuilder.build();
    instance.addEvent(event);

    addEventToMatchingTrainInstance(instance, timestamp, berthAreaId, event);
  }

  private void processBerthCancelMessage(long fileTimestamp,
      BerthCancelMessage message, String source) {
    long timestamp = pickBestTimestamp(message.getTimeAsLong(), fileTimestamp);
    TrainReportingNumberInstance instance = getTrainReportingNumberInstance(
        message.getDescr(), timestamp);
    instance.setLastUpdateTime(timestamp);

    String berthAreaId = message.getAreaId();

    SerializedNarrative.Event.Builder eventBuilder = SerializedNarrative.Event.newBuilder();
    eventBuilder.setTimestamp(timestamp);
    eventBuilder.setSource(source);
    SerializedNarrative.BerthCancelEvent.Builder berthCancelEvent = eventBuilder.getBerthCancelBuilder();
    berthCancelEvent.setFromBerthId(berthAreaId + "_" + message.getFrom());
    SerializedNarrative.Event event = eventBuilder.build();
    instance.addEvent(event);

    addEventToMatchingTrainInstance(instance, timestamp, berthAreaId, event);
  }

  private void processBerthInterposeMessage(long fileTimestamp,
      BerthInterposeMessage message, String source) {
    long timestamp = pickBestTimestamp(message.getTimeAsLong(), fileTimestamp);
    TrainReportingNumberInstance instance = getTrainReportingNumberInstance(
        message.getDescr(), timestamp);
    instance.setLastUpdateTime(timestamp);

    String berthAreaId = message.getAreaId();

    SerializedNarrative.Event.Builder eventBuilder = SerializedNarrative.Event.newBuilder();
    eventBuilder.setTimestamp(timestamp);
    eventBuilder.setSource(source);
    SerializedNarrative.BerthInterposeEvent.Builder berthInterposeEvent = eventBuilder.getBerthInterposeBuilder();
    berthInterposeEvent.setToBerthId(berthAreaId + "_" + message.getTo());
    SerializedNarrative.Event event = eventBuilder.build();
    instance.addEvent(event);

    addEventToMatchingTrainInstance(instance, timestamp, berthAreaId, event);
  }

  private void addEventToMatchingTrainInstance(
      TrainReportingNumberInstance instance, long timestamp,
      String berthAreaId, SerializedNarrative.Event event) {
    Set<String> stanoxAreaIds = _timetableService.getStanoxAreasForBerthArea(berthAreaId);
    if (stanoxAreaIds == null) {
      _log.warn("berthAreaId=" + berthAreaId + " has no known stanox area ids");
      return;
    }

    List<TrainInstance> matchingInstances = new ArrayList<TrainInstance>();
    for (TrainInstance trainInstance : instance.getAllTrainInstances()) {
      if (hasPotentialAreaMatch(trainInstance, timestamp, stanoxAreaIds)) {
        matchingInstances.add(trainInstance);
      }
    }

    if (matchingInstances.size() == 1) {
      TrainInstance trainInstance = matchingInstances.get(0);
      trainInstance.addEvent(event);
      _statisticsService.incrementMatchedBerthStep();
    } else {
      instance.addUnmatchedEvent(event);
      _statisticsService.incrementUnmatchedBerthStep();
    }
  }

  private boolean hasPotentialAreaMatch(TrainInstance trainInstance, long time,
      Set<String> stanoxAreaIds) {
    BasicScheduleElement schedule = trainInstance.getSchedule();
    if (schedule == null) {
      return false;
    }

    if (trainInstance.getLastStanox() != 0
        && Math.abs(time - trainInstance.getLastStanoxTime()) < _timepointMatchThreshold
        && hasStanoxAreaMatch(trainInstance.getLastStanox(), stanoxAreaIds)) {
      return true;
    }

    List<TimepointElement> timepoints = schedule.getTimepoints();
    if (timepoints.isEmpty()) {
      return false;
    }

    TimepointElement firstTimepoint = timepoints.get(0);
    long firstTime = getTimeForTimepoint(trainInstance, firstTimepoint);
    TimepointElement lastTimepoint = timepoints.get(timepoints.size() - 1);
    long lastTime = getTimeForTimepoint(trainInstance, lastTimepoint);
    if (time <= firstTime) {
      if (firstTime - time < _timepointMatchThreshold
          && hasStanoxAreaMatch(firstTimepoint, stanoxAreaIds)) {
        return true;
      }
    } else if (time >= lastTime) {
      if (time - lastTime < _timepointMatchThreshold
          && hasStanoxAreaMatch(lastTimepoint, stanoxAreaIds)) {
        return true;
      }
    } else {
      for (int i = 0; i < timepoints.size() - 1; ++i) {
        TimepointElement from = timepoints.get(i);
        TimepointElement to = timepoints.get(i + 1);
        long fromTime = getTimeForTimepoint(trainInstance, from);
        long toTime = getTimeForTimepoint(trainInstance, to);
        if (fromTime <= time && time <= toTime) {
          return hasStanoxAreaMatch(from, stanoxAreaIds)
              || hasStanoxAreaMatch(to, stanoxAreaIds);
        }
      }
    }
    return false;
  }

  private boolean hasStanoxAreaMatch(TimepointElement element,
      Set<String> stanoxAreaIds) {
    int stanox = _timetableService.getStanoxForTiploc(element.getTiploc());
    return hasStanoxAreaMatch(stanox, stanoxAreaIds);
  }

  private boolean hasStanoxAreaMatch(int stanox, Set<String> stanoxAreaIds) {
    if (stanox != 0) {
      String stanoxArea = _timetableService.getAreaForStanox(stanox);
      if (stanoxAreaIds.contains(stanoxArea)) {
        return true;
      }
    }
    return false;
  }

  private long getTimeForTimepoint(TrainInstance trainInstance,
      TimepointElement element) {
    return trainInstance.getServiceDate()
        + (element.getBestTime() + trainInstance.getScheduleDeviation()) * 1000;
  }

  private TrainReportingNumberInstance getTrainReportingNumberInstance(
      String trainReportingNumber, long time) {
    if (trainReportingNumber == null) {
      throw new IllegalArgumentException("trainReportingNumber is null");
    }
    TrainReportingNumberInstance instance = _trainReportingNumberInstances.get(trainReportingNumber);
    if (instance == null
        || instance.getLastUpdateTime() + _pruneTimeout < _lastUpdateTime) {
      if (instance != null) {
        _trainReportingNumberInstances.remove(trainReportingNumber);
        for (TrainTrackingListener listener : _listeners) {
          for (TrainInstance trainInstance : instance.getAllTrainInstances()) {
            listener.handlePrunedTrainInstance(trainInstance);
          }
          listener.handlePrunedTrainReportingNumberInstance(instance);
        }
      }
      instance = new TrainReportingNumberInstance(trainReportingNumber);
      _trainReportingNumberInstances.put(trainReportingNumber, instance);
    }
    instance.setLastUpdateTime(time);
    if (time + 60 * 60 * 1000 < _lastUpdateTime) {
      _log.warn("timestamp going backwards: from="
          + _format.format(new Date(_lastUpdateTime)) + " to="
          + _format.format(new Date(time)));
      _lastPruneTimestamp = 0;
    }
    _lastUpdateTime = time;
    return instance;
  }

  private void checkForPrune() {
    if (_lastPruneTimestamp + _pruneFrequency < _lastUpdateTime) {
      pruneStaleInstances(false);
    }
  }

  private void pruneStaleInstances(boolean pruneAll) {

    for (Iterator<TrainReportingNumberInstance> it = _trainReportingNumberInstances.values().iterator(); it.hasNext();) {
      TrainReportingNumberInstance instance = it.next();
      if (pruneAll
          || instance.getLastUpdateTime() + _pruneTimeout < _lastUpdateTime) {
        it.remove();
        for (TrainTrackingListener listener : _listeners) {
          for (TrainInstance trainInstance : instance.getAllTrainInstances()) {
            listener.handlePrunedTrainInstance(trainInstance);
          }
          listener.handlePrunedTrainReportingNumberInstance(instance);
        }
        continue;
      }

      for (Iterator<TrainInstance> tit = instance.getAllTrainInstances().iterator(); tit.hasNext();) {
        TrainInstance trainInstance = tit.next();
        if (pruneAll
            || trainInstance.getLastUpdateTime() + _pruneTimeout < _lastUpdateTime) {
          tit.remove();
          for (TrainTrackingListener listener : _listeners) {
            listener.handlePrunedTrainInstance(trainInstance);
          }
        }
      }
    }
    _lastPruneTimestamp = _lastUpdateTime;
  }

}
