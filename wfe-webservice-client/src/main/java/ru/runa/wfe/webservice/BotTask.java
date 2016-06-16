
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for botTask complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="botTask">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="bot" type="{http://impl.service.wfe.runa.ru/}bot" minOccurs="0"/>
 *         &lt;element name="configuration" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="createDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="embeddedFile" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="embeddedFileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="sequentialExecution" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="taskHandlerClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "botTask", propOrder = {
    "bot",
    "configuration",
    "createDate",
    "embeddedFile",
    "embeddedFileName",
    "id",
    "name",
    "sequentialExecution",
    "taskHandlerClassName",
    "version"
})
public class BotTask {

    protected Bot bot;
    protected byte[] configuration;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createDate;
    protected byte[] embeddedFile;
    protected String embeddedFileName;
    protected Long id;
    protected String name;
    protected Boolean sequentialExecution;
    protected String taskHandlerClassName;
    protected Long version;

    /**
     * Gets the value of the bot property.
     * 
     * @return
     *     possible object is
     *     {@link Bot }
     *     
     */
    public Bot getBot() {
        return bot;
    }

    /**
     * Sets the value of the bot property.
     * 
     * @param value
     *     allowed object is
     *     {@link Bot }
     *     
     */
    public void setBot(Bot value) {
        this.bot = value;
    }

    /**
     * Gets the value of the configuration property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getConfiguration() {
        return configuration;
    }

    /**
     * Sets the value of the configuration property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setConfiguration(byte[] value) {
        this.configuration = ((byte[]) value);
    }

    /**
     * Gets the value of the createDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreateDate() {
        return createDate;
    }

    /**
     * Sets the value of the createDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreateDate(XMLGregorianCalendar value) {
        this.createDate = value;
    }

    /**
     * Gets the value of the embeddedFile property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getEmbeddedFile() {
        return embeddedFile;
    }

    /**
     * Sets the value of the embeddedFile property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setEmbeddedFile(byte[] value) {
        this.embeddedFile = ((byte[]) value);
    }

    /**
     * Gets the value of the embeddedFileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmbeddedFileName() {
        return embeddedFileName;
    }

    /**
     * Sets the value of the embeddedFileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmbeddedFileName(String value) {
        this.embeddedFileName = value;
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
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the sequentialExecution property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSequentialExecution() {
        return sequentialExecution;
    }

    /**
     * Sets the value of the sequentialExecution property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSequentialExecution(Boolean value) {
        this.sequentialExecution = value;
    }

    /**
     * Gets the value of the taskHandlerClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaskHandlerClassName() {
        return taskHandlerClassName;
    }

    /**
     * Sets the value of the taskHandlerClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaskHandlerClassName(String value) {
        this.taskHandlerClassName = value;
    }

    /**
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setVersion(Long value) {
        this.version = value;
    }

}
