
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for processFilter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processFilter">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="definitionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="definitionVersion" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="endDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="endDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="failedOnly" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="finished" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="idFrom" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="idTo" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="startDateFrom" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="startDateTo" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processFilter", propOrder = {
    "definitionName",
    "definitionVersion",
    "endDateFrom",
    "endDateTo",
    "failedOnly",
    "finished",
    "id",
    "idFrom",
    "idTo",
    "startDateFrom",
    "startDateTo"
})
public class ProcessFilter {

    protected String definitionName;
    protected Long definitionVersion;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar endDateTo;
    protected boolean failedOnly;
    protected Boolean finished;
    protected Long id;
    protected Long idFrom;
    protected Long idTo;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDateFrom;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar startDateTo;

    /**
     * Gets the value of the definitionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefinitionName() {
        return definitionName;
    }

    /**
     * Sets the value of the definitionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefinitionName(String value) {
        this.definitionName = value;
    }

    /**
     * Gets the value of the definitionVersion property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDefinitionVersion() {
        return definitionVersion;
    }

    /**
     * Sets the value of the definitionVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDefinitionVersion(Long value) {
        this.definitionVersion = value;
    }

    /**
     * Gets the value of the endDateFrom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDateFrom() {
        return endDateFrom;
    }

    /**
     * Sets the value of the endDateFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDateFrom(XMLGregorianCalendar value) {
        this.endDateFrom = value;
    }

    /**
     * Gets the value of the endDateTo property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getEndDateTo() {
        return endDateTo;
    }

    /**
     * Sets the value of the endDateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setEndDateTo(XMLGregorianCalendar value) {
        this.endDateTo = value;
    }

    /**
     * Gets the value of the failedOnly property.
     * 
     */
    public boolean isFailedOnly() {
        return failedOnly;
    }

    /**
     * Sets the value of the failedOnly property.
     * 
     */
    public void setFailedOnly(boolean value) {
        this.failedOnly = value;
    }

    /**
     * Gets the value of the finished property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isFinished() {
        return finished;
    }

    /**
     * Sets the value of the finished property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setFinished(Boolean value) {
        this.finished = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the idFrom property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIdFrom() {
        return idFrom;
    }

    /**
     * Sets the value of the idFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdFrom(Long value) {
        this.idFrom = value;
    }

    /**
     * Gets the value of the idTo property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIdTo() {
        return idTo;
    }

    /**
     * Sets the value of the idTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdTo(Long value) {
        this.idTo = value;
    }

    /**
     * Gets the value of the startDateFrom property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDateFrom() {
        return startDateFrom;
    }

    /**
     * Sets the value of the startDateFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDateFrom(XMLGregorianCalendar value) {
        this.startDateFrom = value;
    }

    /**
     * Gets the value of the startDateTo property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getStartDateTo() {
        return startDateTo;
    }

    /**
     * Sets the value of the startDateTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setStartDateTo(XMLGregorianCalendar value) {
        this.startDateTo = value;
    }

}
