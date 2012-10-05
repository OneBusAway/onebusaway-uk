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
import java.io.PrintWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import uk.org.naptan.AnnotatedRailRefStructure;
import uk.org.naptan.LocationStructure.Translation;
import uk.org.naptan.NaPTAN;
import uk.org.naptan.StopClassificationStructure;
import uk.org.naptan.StopClassificationStructure.OffStreet;
import uk.org.naptan.StopClassificationStructure.OffStreet.Rail;
import uk.org.naptan.StopPointStructure;
import uk.org.naptan.StopPointStructure.Place;
import uk.org.naptan.StopPointStructure.Place.Location;
import uk.org.naptan.StopPointsStructure;

public class StationMappingMain {
  public static void main(String[] args) throws Exception {
    JAXBContext context = JAXBContext.newInstance("uk.org.naptan");
    Unmarshaller unmarshaller = context.createUnmarshaller();
    NaPTAN result = (NaPTAN) unmarshaller.unmarshal(new File(
        "/Users/bdferris/Documents/uk-rail/naptan/NaPTAN.xml"));

    PrintWriter writer = new PrintWriter(new File(
        "/Users/bdferris/Documents/uk-rail/naptan/stations.xml"));
    writer.println("tiploc,lat,lon");
    StopPointsStructure stopPoints = result.getStopPoints();
    int i = 0;
    for (StopPointStructure stopPoint : stopPoints.getStopPoint()) {
      String tiploc = getTiplocForStopPoint(stopPoint);
      Double lat = getLatForStopPoint(stopPoint);
      Double lon = getLonForStopPoint(stopPoint);

      if (tiploc != null) {
        writer.println(tiploc + "," + lat + "," + lon);
        i++;
      }
    }
    writer.close();
    System.out.println(i + " stations written");

  }

  private static String getTiplocForStopPoint(StopPointStructure stopPoint) {

    StopClassificationStructure classification = stopPoint.getStopClassification();
    OffStreet offstreet = classification.getOffStreet();
    if (offstreet == null) {
      return null;
    }
    Rail rail = offstreet.getRail();
    if (rail == null) {
      return null;
    }
    for (AnnotatedRailRefStructure ref : rail.getAnnotatedRailRef()) {
      if (ref.getTiplocRef() != null) {
        return ref.getTiplocRef();
      }
    }
    return null;
  }

  private static Double getLatForStopPoint(StopPointStructure stopPoint) {
    Place place = stopPoint.getPlace();
    Location location = place.getLocation();
    if (location.getLatitude() != null) {
      return location.getLatitude().doubleValue();
    }
    Translation translation = location.getTranslation();
    if (translation.getLatitude() != null) {
      return translation.getLatitude().doubleValue();
    }
    return null;
  }

  private static Double getLonForStopPoint(StopPointStructure stopPoint) {
    Place place = stopPoint.getPlace();
    Location location = place.getLocation();
    if (location.getLongitude() != null) {
      return location.getLongitude().doubleValue();
    }
    Translation translation = location.getTranslation();
    if (translation.getLongitude() != null) {
      return translation.getLongitude().doubleValue();
    }
    return null;
  }
}
