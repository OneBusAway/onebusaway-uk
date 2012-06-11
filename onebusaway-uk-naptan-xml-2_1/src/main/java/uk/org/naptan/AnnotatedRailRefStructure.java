//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.09 at 02:09:56 PM CEST 
//


package uk.org.naptan;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Collation with rail industry reference systems
 * 
 * <p>Java class for AnnotatedRailRefStructure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AnnotatedRailRefStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="TiplocRef" type="{http://www.naptan.org.uk/}TiplocCodeType"/>
 *         &lt;element name="CrsRef" type="{http://www.naptan.org.uk/}CrsCodeType" minOccurs="0"/>
 *         &lt;element name="StationName" type="{http://www.naptan.org.uk/}NaturalLanguageStringStructure"/>
 *         &lt;element name="Location" type="{http://www.naptan.org.uk/}LocationStructure" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.naptan.org.uk/}ModificationDetailsGroup"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AnnotatedRailRefStructure", propOrder = {
    "tiplocRef",
    "crsRef",
    "stationName",
    "location"
})
public class AnnotatedRailRefStructure {

    @XmlElement(name = "TiplocRef", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String tiplocRef;
    @XmlElement(name = "CrsRef")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String crsRef;
    @XmlElement(name = "StationName", required = true)
    protected NaturalLanguageStringStructure stationName;
    @XmlElement(name = "Location")
    protected LocationStructure location;
    @XmlAttribute(name = "CreationDateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creationDateTime;
    @XmlAttribute(name = "ModificationDateTime")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar modificationDateTime;
    @XmlAttribute(name = "Modification")
    protected ModificationEnumeration modification;
    @XmlAttribute(name = "RevisionNumber")
    protected BigInteger revisionNumber;
    @XmlAttribute(name = "Status")
    protected StatusEnumeration status;

    /**
     * Gets the value of the tiplocRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTiplocRef() {
        return tiplocRef;
    }

    /**
     * Sets the value of the tiplocRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTiplocRef(String value) {
        this.tiplocRef = value;
    }

    /**
     * Gets the value of the crsRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrsRef() {
        return crsRef;
    }

    /**
     * Sets the value of the crsRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrsRef(String value) {
        this.crsRef = value;
    }

    /**
     * Gets the value of the stationName property.
     * 
     * @return
     *     possible object is
     *     {@link NaturalLanguageStringStructure }
     *     
     */
    public NaturalLanguageStringStructure getStationName() {
        return stationName;
    }

    /**
     * Sets the value of the stationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link NaturalLanguageStringStructure }
     *     
     */
    public void setStationName(NaturalLanguageStringStructure value) {
        this.stationName = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link LocationStructure }
     *     
     */
    public LocationStructure getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link LocationStructure }
     *     
     */
    public void setLocation(LocationStructure value) {
        this.location = value;
    }

    /**
     * Gets the value of the creationDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreationDateTime() {
        return creationDateTime;
    }

    /**
     * Sets the value of the creationDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreationDateTime(XMLGregorianCalendar value) {
        this.creationDateTime = value;
    }

    /**
     * Gets the value of the modificationDateTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getModificationDateTime() {
        return modificationDateTime;
    }

    /**
     * Sets the value of the modificationDateTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setModificationDateTime(XMLGregorianCalendar value) {
        this.modificationDateTime = value;
    }

    /**
     * Gets the value of the modification property.
     * 
     * @return
     *     possible object is
     *     {@link ModificationEnumeration }
     *     
     */
    public ModificationEnumeration getModification() {
        if (modification == null) {
            return ModificationEnumeration.NEW;
        } else {
            return modification;
        }
    }

    /**
     * Sets the value of the modification property.
     * 
     * @param value
     *     allowed object is
     *     {@link ModificationEnumeration }
     *     
     */
    public void setModification(ModificationEnumeration value) {
        this.modification = value;
    }

    /**
     * Gets the value of the revisionNumber property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getRevisionNumber() {
        return revisionNumber;
    }

    /**
     * Sets the value of the revisionNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setRevisionNumber(BigInteger value) {
        this.revisionNumber = value;
    }

    /**
     * Gets the value of the status property.
     * 
     * @return
     *     possible object is
     *     {@link StatusEnumeration }
     *     
     */
    public StatusEnumeration getStatus() {
        if (status == null) {
            return StatusEnumeration.ACTIVE;
        } else {
            return status;
        }
    }

    /**
     * Sets the value of the status property.
     * 
     * @param value
     *     allowed object is
     *     {@link StatusEnumeration }
     *     
     */
    public void setStatus(StatusEnumeration value) {
        this.status = value;
    }

}
