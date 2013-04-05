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

import java.util.Set;

import org.onebusaway.guice.jsr250.JSR250Module;
import org.onebusaway.status_exporter.StatusJettyExporterModule;
import org.onebusaway.uk.network_rail.gtfs_realtime.instance.TrainTrackingService;
import org.onebusway.gtfs_realtime.exporter.GtfsRealtimeExporterModule;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Provider;

public class NetworkRailGtfsRealtimeModule extends AbstractModule {

  public static void addModuleAndDependencies(Set<Module> modules) {
    modules.add(new NetworkRailGtfsRealtimeModule());
    GtfsRealtimeExporterModule.addModuleAndDependencies(modules);
    StatusJettyExporterModule.addModuleAndDependencies(modules);
    JSR250Module.addModuleAndDependencies(modules);
  }

  @Override
  protected void configure() {
    bind(GtfsRealtimeService.class);
    bind(StatisticsService.class);
    bind(LoggingService.class);
    bind(Gson.class).toProvider(GsonProvider.class);
    bind(MessageHandler.class).to(MessageParserService.class);
    bind(TrainMovementHandler.class).to(TrainTrackingService.class);
    bind(TrainDescriberHandler.class).to(TrainTrackingService.class);
  }

  private static class GsonProvider implements Provider<Gson> {

    @Override
    public Gson get() {
      return new GsonBuilder().setFieldNamingPolicy(
          FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }
  }

  /**
   * Implement hashCode() and equals() such that two instances of the module
   * will be equal.
   */
  @Override
  public int hashCode() {
    return this.getClass().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null)
      return false;
    return this.getClass().equals(o.getClass());
  }
}
