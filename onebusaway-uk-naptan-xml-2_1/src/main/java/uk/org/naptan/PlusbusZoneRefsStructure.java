//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.09 at 02:09:56 PM CEST 
//


package uk.org.naptan;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * A collection of one or more references to Plusbus Zones
 * 
 * <p>Java class for PlusbusZoneRefsStructure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PlusbusZoneRefsStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="PlusbusZoneRef" type="{http://www.naptan.org.uk/}PlusbusZoneVersionedRefStructure" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlusbusZoneRefsStructure", propOrder = {
    "plusbusZoneRef"
})
public class PlusbusZoneRefsStructure {

    @XmlElement(name = "PlusbusZoneRef", required = true)
    protected List<PlusbusZoneVersionedRefStructure> plusbusZoneRef;

    /**
     * Gets the value of the plusbusZoneRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the plusbusZoneRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlusbusZoneRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PlusbusZoneVersionedRefStructure }
     * 
     * 
     */
    public List<PlusbusZoneVersionedRefStructure> getPlusbusZoneRef() {
        if (plusbusZoneRef == null) {
            plusbusZoneRef = new ArrayList<PlusbusZoneVersionedRefStructure>();
        }
        return this.plusbusZoneRef;
    }

}
