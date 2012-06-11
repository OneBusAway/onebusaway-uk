//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.09 at 02:09:56 PM CEST 
//


package uk.org.naptan;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * Data type for Availability of stop for use. Note that the Status attribute on StopPoint should correspond with the StopValidity in effect at the ModificationDateTime. If no explicit stop validity is present, stop is assumed to have validity as indicated by Status attribute indefinitely, 
 * 
 * <p>Java class for StopValidityStructure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="StopValidityStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="StopValidity" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="DateRange" type="{http://www.naptan.org.uk/}HalfOpenDateRangeStructure"/>
 *                   &lt;choice>
 *                     &lt;element name="Active" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                     &lt;element name="Suspended" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                     &lt;element name="Transferred" type="{http://www.naptan.org.uk/}StopPointWrappedRefStructure"/>
 *                   &lt;/choice>
 *                   &lt;element name="Note" type="{http://www.naptan.org.uk/}NaturalLanguageStringStructure" minOccurs="0"/>
 *                 &lt;/sequence>
 *                 &lt;attGroup ref="{http://www.naptan.org.uk/}ModificationDetailsGroup"/>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StopValidityStructure", propOrder = {
    "stopValidity"
})
public class StopValidityStructure {

    @XmlElement(name = "StopValidity", required = true)
    protected List<StopValidityStructure.StopValidity> stopValidity;

    /**
     * Gets the value of the stopValidity property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stopValidity property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStopValidity().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StopValidityStructure.StopValidity }
     * 
     * 
     */
    public List<StopValidityStructure.StopValidity> getStopValidity() {
        if (stopValidity == null) {
            stopValidity = new ArrayList<StopValidityStructure.StopValidity>();
        }
        return this.stopValidity;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="DateRange" type="{http://www.naptan.org.uk/}HalfOpenDateRangeStructure"/>
     *         &lt;choice>
     *           &lt;element name="Active" type="{http://www.naptan.org.uk/}EmptyType"/>
     *           &lt;element name="Suspended" type="{http://www.naptan.org.uk/}EmptyType"/>
     *           &lt;element name="Transferred" type="{http://www.naptan.org.uk/}StopPointWrappedRefStructure"/>
     *         &lt;/choice>
     *         &lt;element name="Note" type="{http://www.naptan.org.uk/}NaturalLanguageStringStructure" minOccurs="0"/>
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
    @XmlType(name = "", propOrder = {
        "dateRange",
        "active",
        "suspended",
        "transferred",
        "note"
    })
    public static class StopValidity {

        @XmlElement(name = "DateRange", required = true)
        protected HalfOpenDateRangeStructure dateRange;
        @XmlElement(name = "Active")
        protected String active;
        @XmlElement(name = "Suspended")
        protected String suspended;
        @XmlElement(name = "Transferred")
        protected StopPointWrappedRefStructure transferred;
        @XmlElement(name = "Note")
        protected NaturalLanguageStringStructure note;
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
         * Gets the value of the dateRange property.
         * 
         * @return
         *     possible object is
         *     {@link HalfOpenDateRangeStructure }
         *     
         */
        public HalfOpenDateRangeStructure getDateRange() {
            return dateRange;
        }

        /**
         * Sets the value of the dateRange property.
         * 
         * @param value
         *     allowed object is
         *     {@link HalfOpenDateRangeStructure }
         *     
         */
        public void setDateRange(HalfOpenDateRangeStructure value) {
            this.dateRange = value;
        }

        /**
         * Gets the value of the active property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getActive() {
            return active;
        }

        /**
         * Sets the value of the active property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setActive(String value) {
            this.active = value;
        }

        /**
         * Gets the value of the suspended property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getSuspended() {
            return suspended;
        }

        /**
         * Sets the value of the suspended property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setSuspended(String value) {
            this.suspended = value;
        }

        /**
         * Gets the value of the transferred property.
         * 
         * @return
         *     possible object is
         *     {@link StopPointWrappedRefStructure }
         *     
         */
        public StopPointWrappedRefStructure getTransferred() {
            return transferred;
        }

        /**
         * Sets the value of the transferred property.
         * 
         * @param value
         *     allowed object is
         *     {@link StopPointWrappedRefStructure }
         *     
         */
        public void setTransferred(StopPointWrappedRefStructure value) {
            this.transferred = value;
        }

        /**
         * Gets the value of the note property.
         * 
         * @return
         *     possible object is
         *     {@link NaturalLanguageStringStructure }
         *     
         */
        public NaturalLanguageStringStructure getNote() {
            return note;
        }

        /**
         * Sets the value of the note property.
         * 
         * @param value
         *     allowed object is
         *     {@link NaturalLanguageStringStructure }
         *     
         */
        public void setNote(NaturalLanguageStringStructure value) {
            this.note = value;
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

}
