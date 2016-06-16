
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for wfDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="wfDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}identifiable">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="categories" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="version" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="hasHtmlDescription" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="hasStartImage" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="hasDisabledImage" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="subprocessOnly" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="canBeStarted" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="createDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="createActor" type="{http://impl.service.wfe.runa.ru/}actor" minOccurs="0"/>
 *         &lt;element name="updateDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="updateActor" type="{http://impl.service.wfe.runa.ru/}actor" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wfDefinition", propOrder = {
    "id",
    "name",
    "description",
    "categories",
    "version",
    "hasHtmlDescription",
    "hasStartImage",
    "hasDisabledImage",
    "subprocessOnly",
    "canBeStarted",
    "createDate",
    "createActor",
    "updateDate",
    "updateActor"
})
public class WfDefinition
    extends Identifiable
{

    protected Long id;
    protected String name;
    protected String description;
    @XmlElement(nillable = true)
    protected List<String> categories;
    protected Long version;
    protected boolean hasHtmlDescription;
    protected boolean hasStartImage;
    protected boolean hasDisabledImage;
    protected boolean subprocessOnly;
    protected boolean canBeStarted;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar createDate;
    protected Actor createActor;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar updateDate;
    protected Actor updateActor;

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
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the categories property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the categories property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCategories().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getCategories() {
        if (categories == null) {
            categories = new ArrayList<String>();
        }
        return this.categories;
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

    /**
     * Gets the value of the hasHtmlDescription property.
     * 
     */
    public boolean isHasHtmlDescription() {
        return hasHtmlDescription;
    }

    /**
     * Sets the value of the hasHtmlDescription property.
     * 
     */
    public void setHasHtmlDescription(boolean value) {
        this.hasHtmlDescription = value;
    }

    /**
     * Gets the value of the hasStartImage property.
     * 
     */
    public boolean isHasStartImage() {
        return hasStartImage;
    }

    /**
     * Sets the value of the hasStartImage property.
     * 
     */
    public void setHasStartImage(boolean value) {
        this.hasStartImage = value;
    }

    /**
     * Gets the value of the hasDisabledImage property.
     * 
     */
    public boolean isHasDisabledImage() {
        return hasDisabledImage;
    }

    /**
     * Sets the value of the hasDisabledImage property.
     * 
     */
    public void setHasDisabledImage(boolean value) {
        this.hasDisabledImage = value;
    }

    /**
     * Gets the value of the subprocessOnly property.
     * 
     */
    public boolean isSubprocessOnly() {
        return subprocessOnly;
    }

    /**
     * Sets the value of the subprocessOnly property.
     * 
     */
    public void setSubprocessOnly(boolean value) {
        this.subprocessOnly = value;
    }

    /**
     * Gets the value of the canBeStarted property.
     * 
     */
    public boolean isCanBeStarted() {
        return canBeStarted;
    }

    /**
     * Sets the value of the canBeStarted property.
     * 
     */
    public void setCanBeStarted(boolean value) {
        this.canBeStarted = value;
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
     * Gets the value of the createActor property.
     * 
     * @return
     *     possible object is
     *     {@link Actor }
     *     
     */
    public Actor getCreateActor() {
        return createActor;
    }

    /**
     * Sets the value of the createActor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actor }
     *     
     */
    public void setCreateActor(Actor value) {
        this.createActor = value;
    }

    /**
     * Gets the value of the updateDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getUpdateDate() {
        return updateDate;
    }

    /**
     * Sets the value of the updateDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setUpdateDate(XMLGregorianCalendar value) {
        this.updateDate = value;
    }

    /**
     * Gets the value of the updateActor property.
     * 
     * @return
     *     possible object is
     *     {@link Actor }
     *     
     */
    public Actor getUpdateActor() {
        return updateActor;
    }

    /**
     * Sets the value of the updateActor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actor }
     *     
     */
    public void setUpdateActor(Actor value) {
        this.updateActor = value;
    }

}
