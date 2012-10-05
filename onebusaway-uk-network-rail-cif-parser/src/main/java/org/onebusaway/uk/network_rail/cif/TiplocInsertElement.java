/**
 * Copyright (C) 2012 Google, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.onebusaway.uk.network_rail.cif;

/**
 * @author bdferris
 * 
 */
public class TiplocInsertElement extends CifElement {

  private String tiploc;

  private int tiplocCapitalization;

  /**
   * National Location Code
   */
  private int nalco;

  private String nlcCheckCharacter;

  /**
   * TPS Description of location
   */
  private String tpsDescription;

  /**
   * Value may be zero, indicating an unspecified stanox.
   */
  private int stanox;

  private int postOfficeLocationCode;

  private String crsCode;

  private String capriDescription;

  public TiplocInsertElement() {
    super(CifElement.Type.TIPLOC_INSERT);
  }

  public String getTiploc() {
    return tiploc;
  }

  public void setTiploc(String tiploc) {
    this.tiploc = tiploc;
  }

  public int getTiplocCapitalization() {
    return tiplocCapitalization;
  }

  public void setTiplocCapitalization(int tiplocCapitalization) {
    this.tiplocCapitalization = tiplocCapitalization;
  }

  public int getNalco() {
    return nalco;
  }

  public void setNalco(int nalco) {
    this.nalco = nalco;
  }

  public String getNlcCheckCharacter() {
    return nlcCheckCharacter;
  }

  public void setNlcCheckCharacter(String nlcCheckCharacter) {
    this.nlcCheckCharacter = nlcCheckCharacter;
  }

  public String getTpsDescription() {
    return tpsDescription;
  }

  public void setTpsDescription(String tpsDescription) {
    this.tpsDescription = tpsDescription;
  }

  /**
   * @return the stanox associated with the tiploc or zero if the stanox has not
   *         been specified.
   */
  public int getStanox() {
    return stanox;
  }

  public void setStanox(int stanox) {
    this.stanox = stanox;
  }

  public int getPostOfficeLocationCode() {
    return postOfficeLocationCode;
  }

  public void setPostOfficeLocationCode(int postOfficeLocationCode) {
    this.postOfficeLocationCode = postOfficeLocationCode;
  }

  public String getCrsCode() {
    return crsCode;
  }

  public void setCrsCode(String crsCode) {
    this.crsCode = crsCode;
  }

  public String getCapriDescription() {
    return capriDescription;
  }

  public void setCapriDescription(String capriDescription) {
    this.capriDescription = capriDescription;
  }
}
