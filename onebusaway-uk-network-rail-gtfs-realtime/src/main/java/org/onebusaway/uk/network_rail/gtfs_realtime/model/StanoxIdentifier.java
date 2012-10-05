package org.onebusaway.uk.network_rail.gtfs_realtime.model;

public class StanoxIdentifier extends TrackIdentifier {

  private static final long serialVersionUID = 1L;

  private final int stanox;

  public StanoxIdentifier(int stanox) {
    this.stanox = stanox;
  }

  public int getStanox() {
    return stanox;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + stanox;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    StanoxIdentifier other = (StanoxIdentifier) obj;
    if (stanox != other.stanox)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return Integer.toString(stanox);
  }
}