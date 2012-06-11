//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.09 at 02:09:56 PM CEST 
//


package uk.org.naptan;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Type for Days of operation.
 * 
 * <p>Java class for DaysOfOperationStructure complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DaysOfOperationStructure">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;sequence>
 *             &lt;choice>
 *               &lt;sequence>
 *                 &lt;choice>
 *                   &lt;sequence>
 *                     &lt;element name="Monday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                     &lt;element name="Tuesday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                     &lt;element name="Wednesday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                     &lt;element name="Thursday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                     &lt;element name="Friday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                   &lt;/sequence>
 *                   &lt;element name="MondayToFriday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *                 &lt;/choice>
 *                 &lt;element name="Saturday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *               &lt;/sequence>
 *               &lt;element name="MondayToSaturday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *             &lt;/choice>
 *             &lt;element name="Sunday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *           &lt;/sequence>
 *           &lt;element name="MondayToSunday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="BankHoliday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *           &lt;element name="NotBankHoliday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *         &lt;/choice>
 *         &lt;choice>
 *           &lt;element name="SchoolHoliday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *           &lt;element name="NotSchoolHoliday" type="{http://www.naptan.org.uk/}EmptyType"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DaysOfOperationStructure", propOrder = {
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "mondayToFriday",
    "saturday",
    "mondayToSaturday",
    "sunday",
    "mondayToSunday",
    "bankHoliday",
    "notBankHoliday",
    "schoolHoliday",
    "notSchoolHoliday"
})
public class DaysOfOperationStructure {

    @XmlElement(name = "Monday")
    protected String monday;
    @XmlElement(name = "Tuesday")
    protected String tuesday;
    @XmlElement(name = "Wednesday")
    protected String wednesday;
    @XmlElement(name = "Thursday")
    protected String thursday;
    @XmlElement(name = "Friday")
    protected String friday;
    @XmlElement(name = "MondayToFriday")
    protected String mondayToFriday;
    @XmlElement(name = "Saturday")
    protected String saturday;
    @XmlElement(name = "MondayToSaturday")
    protected String mondayToSaturday;
    @XmlElement(name = "Sunday")
    protected String sunday;
    @XmlElement(name = "MondayToSunday")
    protected String mondayToSunday;
    @XmlElement(name = "BankHoliday")
    protected String bankHoliday;
    @XmlElement(name = "NotBankHoliday")
    protected String notBankHoliday;
    @XmlElement(name = "SchoolHoliday")
    protected String schoolHoliday;
    @XmlElement(name = "NotSchoolHoliday")
    protected String notSchoolHoliday;

    /**
     * Gets the value of the monday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMonday() {
        return monday;
    }

    /**
     * Sets the value of the monday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMonday(String value) {
        this.monday = value;
    }

    /**
     * Gets the value of the tuesday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTuesday() {
        return tuesday;
    }

    /**
     * Sets the value of the tuesday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTuesday(String value) {
        this.tuesday = value;
    }

    /**
     * Gets the value of the wednesday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWednesday() {
        return wednesday;
    }

    /**
     * Sets the value of the wednesday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWednesday(String value) {
        this.wednesday = value;
    }

    /**
     * Gets the value of the thursday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThursday() {
        return thursday;
    }

    /**
     * Sets the value of the thursday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThursday(String value) {
        this.thursday = value;
    }

    /**
     * Gets the value of the friday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFriday() {
        return friday;
    }

    /**
     * Sets the value of the friday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFriday(String value) {
        this.friday = value;
    }

    /**
     * Gets the value of the mondayToFriday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMondayToFriday() {
        return mondayToFriday;
    }

    /**
     * Sets the value of the mondayToFriday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMondayToFriday(String value) {
        this.mondayToFriday = value;
    }

    /**
     * Gets the value of the saturday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSaturday() {
        return saturday;
    }

    /**
     * Sets the value of the saturday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSaturday(String value) {
        this.saturday = value;
    }

    /**
     * Gets the value of the mondayToSaturday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMondayToSaturday() {
        return mondayToSaturday;
    }

    /**
     * Sets the value of the mondayToSaturday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMondayToSaturday(String value) {
        this.mondayToSaturday = value;
    }

    /**
     * Gets the value of the sunday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSunday() {
        return sunday;
    }

    /**
     * Sets the value of the sunday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSunday(String value) {
        this.sunday = value;
    }

    /**
     * Gets the value of the mondayToSunday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMondayToSunday() {
        return mondayToSunday;
    }

    /**
     * Sets the value of the mondayToSunday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMondayToSunday(String value) {
        this.mondayToSunday = value;
    }

    /**
     * Gets the value of the bankHoliday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBankHoliday() {
        return bankHoliday;
    }

    /**
     * Sets the value of the bankHoliday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBankHoliday(String value) {
        this.bankHoliday = value;
    }

    /**
     * Gets the value of the notBankHoliday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotBankHoliday() {
        return notBankHoliday;
    }

    /**
     * Sets the value of the notBankHoliday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotBankHoliday(String value) {
        this.notBankHoliday = value;
    }

    /**
     * Gets the value of the schoolHoliday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSchoolHoliday() {
        return schoolHoliday;
    }

    /**
     * Sets the value of the schoolHoliday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSchoolHoliday(String value) {
        this.schoolHoliday = value;
    }

    /**
     * Gets the value of the notSchoolHoliday property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotSchoolHoliday() {
        return notSchoolHoliday;
    }

    /**
     * Sets the value of the notSchoolHoliday property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotSchoolHoliday(String value) {
        this.notSchoolHoliday = value;
    }

}
