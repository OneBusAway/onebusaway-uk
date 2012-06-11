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
 * <p>Java class for RelativeBearingEnumeration.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="RelativeBearingEnumeration">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN">
 *     &lt;enumeration value="left"/>
 *     &lt;enumeration value="right"/>
 *     &lt;enumeration value="straightAhead"/>
 *     &lt;enumeration value="uTurn"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "RelativeBearingEnumeration")
@XmlEnum
public enum RelativeBearingEnumeration {

    @XmlEnumValue("left")
    LEFT("left"),
    @XmlEnumValue("right")
    RIGHT("right"),
    @XmlEnumValue("straightAhead")
    STRAIGHT_AHEAD("straightAhead"),
    @XmlEnumValue("uTurn")
    U_TURN("uTurn");
    private final String value;

    RelativeBearingEnumeration(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelativeBearingEnumeration fromValue(String v) {
        for (RelativeBearingEnumeration c: RelativeBearingEnumeration.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
