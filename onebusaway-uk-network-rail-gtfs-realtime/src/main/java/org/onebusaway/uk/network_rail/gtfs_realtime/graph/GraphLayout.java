package org.onebusaway.uk.network_rail.gtfs_realtime.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class GraphLayout {

  private static Logger _log = LoggerFactory.getLogger(GraphLayout.class);

  private int _maxIterations = 10;

  public void updateLocation(Map<Location, Integer> locationsAndWeights,
      Location location, double epsilon) {
    if (locationsAndWeights.size() <= 1) {
      return;
    }

    computeCentroid(locationsAndWeights.keySet(), location);

    Location maxLocation = null;
    int maxWeight = 0;
    for (Map.Entry<Location, Integer> entry : locationsAndWeights.entrySet()) {
      int weight = entry.getValue();
      if (weight > maxWeight) {
        maxWeight = weight;
        maxLocation = entry.getKey();
      }
    }

    for (int i = 0; i < _maxIterations; ++i) {

      Map<Location, Double> distancesByLocation = new HashMap<Location, Double>();
      double maxDistance = 0;
      for (Map.Entry<Location, Integer> entry : locationsAndWeights.entrySet()) {
        Location otherLocation = entry.getKey();
        double dx = otherLocation.x - location.x;
        double dy = otherLocation.y - location.y;
        double d = Math.sqrt(dx * dx + dy * dy);
        distancesByLocation.put(otherLocation, d);
        maxDistance = Math.max(d, maxDistance);
      }
      double referenceDistance = distancesByLocation.get(maxLocation);
      double vx = 0;
      double vy = 0;
      for (Map.Entry<Location, Double> entry : distancesByLocation.entrySet()) {
        Location otherLocation = entry.getKey();
        double distance = distancesByLocation.get(otherLocation);
        double ratio = (double) locationsAndWeights.get(otherLocation)
            / maxWeight;
        double delta = distance - ratio * referenceDistance;
        double dx = (otherLocation.x - location.x) * delta / distance;
        double dy = (otherLocation.y - location.y) * delta / distance;
        vx += dx;
        vy += dy;
      }

      vx /= 2;
      vy /= 2;

      double vDistance = Math.sqrt(vx * vx + vy * vy);
      if (vDistance / maxDistance < epsilon / 2) {
        return;
      }

      location.x += vx;
      location.y += vy;

    }
    _log.warn("max number of iterations reached");
  }

  public void computeCentroid(Set<Location> locations, Location location) {
    double xTotal = 0;
    double yTotal = 0;
    for (Location otherLocation : locations) {
      xTotal += otherLocation.x;
      yTotal += otherLocation.y;
    }
    location.x = xTotal / locations.size();
    location.y = yTotal / locations.size();
  }
}
