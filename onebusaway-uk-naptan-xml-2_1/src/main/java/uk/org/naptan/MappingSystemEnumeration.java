//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.09 at 02:09:56 PM CEST 
//


package uk.org.naptan;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for MappingSystemEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="MappingSystemEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="TOID"/>
 *     &lt;enumeration value="OSODR"/>
 *     &lt;enumeration value="NavTech"/>
 *     &lt;enumeration value="MDV"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "MappingSystemEnumeration")
@XmlEnum
public enum MappingSystemEnumeration {


    /**
     * Ordnance Survey MasterMap Topographical ID.
     * 
     */
    TOID("TOID"),

    /**
     * Ordnance Survey ROADS (previously OSCAR hence the O) Database Reference.
     * 
     */
    OSODR("OSODR"),

    /**
     * NavTech mapping reference.
     * 
     */
    @XmlEnumValue("NavTech")
    NAV_TECH("NavTech"),

    /**
     * Mentz Datenverarbeitung GmbH
     * mapping reference http://www.mentzdv.de
     * .
     * 
     */
    MDV("MDV");
    private final String value;

    MappingSystemEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MappingSystemEnumeration fromValue(String v) {
        for (MappingSystemEnumeration c: MappingSystemEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
