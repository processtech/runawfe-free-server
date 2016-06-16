
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for processError complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="processError">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="nodeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taskName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="botTask" type="{http://impl.service.wfe.runa.ru/}botTask" minOccurs="0"/>
 *         &lt;element name="occurredDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="throwableMessage" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="throwableDetails" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "processError", propOrder = {
    "processId",
    "nodeId",
    "taskName",
    "botTask",
    "occurredDate",
    "throwableMessage",
    "throwableDetails"
})
public class ProcessError {

    protected Long processId;
    protected String nodeId;
    protected String taskName;
    protected BotTask botTask;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar occurredDate;
    protected String throwableMessage;
    protected String throwableDetails;

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setProcessId(Long value) {
        this.processId = value;
    }

    /**
     * Gets the value of the nodeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Sets the value of the nodeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodeId(String value) {
        this.nodeId = value;
    }

    /**
     * Gets the value of the taskName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets the value of the taskName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskName(String value) {
        this.taskName = value;
    }

    /**
     * Gets the value of the botTask property.
     * 
     * @return
     *     possible object is
     *     {@link BotTask }
     *     
     */
    public BotTask getBotTask() {
        return botTask;
    }

    /**
     * Sets the value of the botTask property.
     * 
     * @param value
     *     allowed object is
     *     {@link BotTask }
     *     
     */
    public void setBotTask(BotTask value) {
        this.botTask = value;
    }

    /**
     * Gets the value of the occurredDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOccurredDate() {
        return occurredDate;
    }

    /**
     * Sets the value of the occurredDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOccurredDate(XMLGregorianCalendar value) {
        this.occurredDate = value;
    }

    /**
     * Gets the value of the throwableMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThrowableMessage() {
        return throwableMessage;
    }

    /**
     * Sets the value of the throwableMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThrowableMessage(String value) {
        this.throwableMessage = value;
    }

    /**
     * Gets the value of the throwableDetails property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getThrowableDetails() {
        return throwableDetails;
    }

    /**
     * Sets the value of the throwableDetails property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setThrowableDetails(String value) {
        this.throwableDetails = value;
    }

}
