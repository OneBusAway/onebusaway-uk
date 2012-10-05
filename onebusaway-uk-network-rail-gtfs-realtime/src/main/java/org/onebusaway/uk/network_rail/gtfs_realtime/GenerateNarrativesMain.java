package org.onebusaway.uk.network_rail.gtfs_realtime;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.cli.PosixParser;
import org.onebusaway.guice.jsr250.LifecycleService;
import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainInstance;
import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainReportingNumberInstance;
import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainTrackingListener;
import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainTrackingService;
import org.onebusaway.uk.network_rail.gtfs_realtime.model.SerializedNarrative;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public class GenerateNarrativesMain {

  private static final Logger _log = LoggerFactory.getLogger(GenerateNarrativesMain.class);

  private static final String ARG_LOG_PATH = "logPath";

  private static final String ARG_ATOC_TIMETABLE_PATH = "atocTimetablePath";

  private static final String ARG_BERTH_TO_STANOX_AREA_MAPPING_PATH = "berthToStanoxAreaMappingPath";

  private static final String ARG_NARRATIVE_PB_PATH = "narrativePbPath";

  private static final String ARG_NARRATIVE_ASCII_PATH = "narrativeAsciiPath";

  private static final EventComparator _eventComparator = new EventComparator();

  public static void main(String[] args) throws ParseException, IOException {
    GenerateNarrativesMain m = new GenerateNarrativesMain();
    m.run(args);
  }

  private TimetableService _timetableService;

  private LogReplayService _logReplayService;

  private TrainTrackingService _trainTrackingService;

  private LifecycleService _lifecycleService;

  private String _narrativePbPath;

  private String _narrativeAsciiPath;

  @Inject
  public void setTimetableService(TimetableService timetableService) {
    _timetableService = timetableService;
  }

  @Inject
  public void setLogReplaceService(LogReplayService logReplayService) {
    _logReplayService = logReplayService;
  }

  @Inject
  public void setTrainTrackingService(TrainTrackingService trainTrackingService) {
    _trainTrackingService = trainTrackingService;
  }

  @Inject
  public void setLifecycleService(LifecycleService lifecycleService) {
    _lifecycleService = lifecycleService;
  }

  private void run(String[] args) throws ParseException, IOException {
    Options options = new Options();
    buildOptions(options);

    Parser parser = new PosixParser();
    CommandLine cli = parser.parse(options, args);

    Set<Module> modules = new HashSet<Module>();
    NetworkRailGtfsRealtimeModule.addModuleAndDependencies(modules);
    Injector injector = Guice.createInjector(modules);
    injector.injectMembers(this);

    _timetableService.readScheduleData(new File(
        cli.getOptionValue(ARG_ATOC_TIMETABLE_PATH)));
    _timetableService.readBerthToStanoxAreaMapping(new File(
        cli.getOptionValue(ARG_BERTH_TO_STANOX_AREA_MAPPING_PATH)));

    _trainTrackingService.addListener(new TrainTrackingListenerImpl());

    _narrativePbPath = cli.getOptionValue(ARG_NARRATIVE_PB_PATH);
    _narrativeAsciiPath = cli.getOptionValue(ARG_NARRATIVE_ASCII_PATH);

    _lifecycleService.start();

    File logPath = new File(cli.getOptionValue(ARG_LOG_PATH));
    FileMatcher matcher = new FileMatcher();
    matcher.setExtension(".json");
    // matcher.setMinLastModifiedTime(System.currentTimeMillis()
    // - (43 * 24 * 60 * 60 * 1000L));
    List<File> logFiles = matcher.matchFiles(logPath);
    _log.info("files=" + logFiles.size());
    _logReplayService.parseLogFiles(logPath, logFiles);

    _trainTrackingService.pruneAllInstances();
  }

  private void buildOptions(Options options) {
    options.addOption(ARG_LOG_PATH, true, "log path");
    options.addOption(ARG_ATOC_TIMETABLE_PATH, true, "atoc timetable path");
    options.addOption(ARG_BERTH_TO_STANOX_AREA_MAPPING_PATH, true,
        "berth to stanox mapping path");
    options.addOption(ARG_NARRATIVE_PB_PATH, true, "train pb log path");
    options.addOption(ARG_NARRATIVE_ASCII_PATH, true, "train ascii log path");
  }

  private void logTrains(TrainReportingNumberInstance instance)
      throws IOException {
    List<SerializedNarrative.Event> events = instance.getEvents();
    Collections.sort(events, _eventComparator);

    Date start = new Date(events.get(0).getTimestamp());
    File pbPath = new File(String.format(_narrativePbPath, start,
        instance.getTrainReportingNumber()));
    pbPath.getParentFile().mkdirs();

    SerializedNarrative.TrainInstance.Builder builder = SerializedNarrative.TrainInstance.newBuilder();
    builder.setTrainReportingNumber(instance.getTrainReportingNumber());
    builder.addAllEvent(events);
    SerializedNarrative.TrainInstance serializedTrainInstance = builder.build();
    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
        pbPath));
    serializedTrainInstance.writeTo(out);
    out.close();

    File asciiPath = new File(String.format(_narrativeAsciiPath, start,
        instance.getTrainReportingNumber()));
    BufferedOutputStream asciiOut = new BufferedOutputStream(
        new FileOutputStream(asciiPath));
    asciiOut.write(serializedTrainInstance.toString().getBytes());
    asciiOut.close();
  }

  private void logTrain(TrainInstance trainInstance) throws IOException {
    List<SerializedNarrative.Event> events = trainInstance.getEvents();
    Collections.sort(events, _eventComparator);

    SerializedNarrative.TrainInstance.Builder builder = SerializedNarrative.TrainInstance.newBuilder();
    builder.setTrainId(trainInstance.getTrainId());
    builder.setTrainReportingNumber(trainInstance.getTrainReportingNumber());
    if (trainInstance.getSchedule() != null) {
      builder.setTrainUid(trainInstance.getSchedule().getTrainUid());
    }
    builder.addAllEvent(events);
    SerializedNarrative.TrainInstance serializedTrainInstance = builder.build();

    writePbLogFile(serializedTrainInstance);
    writeAsciiLogFile(serializedTrainInstance);
  }

  private void writePbLogFile(
      SerializedNarrative.TrainInstance serializedTrainInstance)
      throws FileNotFoundException, IOException {
    Date start = new Date(serializedTrainInstance.getEvent(0).getTimestamp());
    File path = new File(String.format(_narrativePbPath, start,
        serializedTrainInstance.getTrainId()));
    path.getParentFile().mkdirs();

    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
        path));
    serializedTrainInstance.writeTo(out);
    out.close();
  }

  private void writeAsciiLogFile(
      SerializedNarrative.TrainInstance serializedTrainInstance)
      throws FileNotFoundException, IOException {
    Date start = new Date(serializedTrainInstance.getEvent(0).getTimestamp());
    File path = new File(String.format(_narrativeAsciiPath, start,
        serializedTrainInstance.getTrainId()));
    path.getParentFile().mkdirs();

    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(
        path));
    out.write(serializedTrainInstance.toString().getBytes());
    out.close();
  }

  private class TrainTrackingListenerImpl implements TrainTrackingListener {

    @Override
    public void handlePrunedTrainReportingNumberInstance(
        TrainReportingNumberInstance instance) {
      try {
        logTrains(instance);
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }

    @Override
    public void handlePrunedTrainInstance(TrainInstance trainInstance) {
      try {
        logTrain(trainInstance);
      } catch (IOException ex) {
        throw new IllegalStateException(ex);
      }
    }
  }

  private static class EventComparator implements
      Comparator<SerializedNarrative.Event> {

    @Override
    public int compare(SerializedNarrative.Event o1,
        SerializedNarrative.Event o2) {
      return o1.getTimestamp() == o2.getTimestamp() ? 0
          : (o1.getTimestamp() < o2.getTimestamp() ? -1 : 1);
    }
  }
}
