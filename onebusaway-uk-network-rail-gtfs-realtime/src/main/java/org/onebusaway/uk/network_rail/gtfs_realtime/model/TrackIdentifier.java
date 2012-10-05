package org.onebusaway.uk.network_rail.gtfs_realtime.model;

import java.io.Serializable;

public abstract class TrackIdentifier implements Serializable {

  private static final long serialVersionUID = 1L;

  public static TrackIdentifier getStanoxIdentifier(int stanox) {
    return new StanoxIdentifier(stanox);
  }

  public static BerthStepIdentifier getBerthIdentifier(String combinedBerthId) {
    String[] tokens = combinedBerthId.split("-");
    return getBerthIdentifier(tokens[0], tokens[1]);
  }
  
  public static BerthStepIdentifier getBerthIdentifier(String fromBerthId,
      String toBerthId) {
    return new BerthStepIdentifier(fromBerthId, toBerthId);
  }
}
