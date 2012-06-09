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
package org.onebusaway.uk.parser;

import java.awt.geom.Point2D;

import com.jhlabs.map.proj.CoordinateSystemToCoordinateSystem;
import com.jhlabs.map.proj.Projection;
import com.jhlabs.map.proj.ProjectionFactory;

public class ProjectionSupport {

  private static final String _fromProjectionSpec = "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 "
      + "+y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.060,0.1502,0.2470,0.8421,-20.4894 +datum=OSGB36  +units=m +no_defs";

  private static final Projection _fromProjection = ProjectionFactory.fromPROJ4Specification(_fromProjectionSpec.split(" "));

  private static final String _toProjectionSpec = "+proj=latlong +ellps=WGS84 +datum=WGS84 +no_defs";

  private static final Projection _toProjection = ProjectionFactory.fromPROJ4Specification(_toProjectionSpec.split(" "));

  public static Point2D.Double convertToLatLon(double x, double y) {
    return convertToLatLon(x, y, new Point2D.Double());
  }

  public static Point2D.Double convertToLatLon(double x, double y,
      Point2D.Double result) {
    Point2D.Double from = new Point2D.Double(x, y);
    CoordinateSystemToCoordinateSystem.transform(_fromProjection,
        _toProjection, from, result);
    return result;
  }
}
