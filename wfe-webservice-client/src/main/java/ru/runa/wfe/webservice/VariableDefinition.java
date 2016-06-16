
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for variableDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="variableDefinition">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="synthetic" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="scriptingName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formatLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="userType" type="{http://impl.service.wfe.runa.ru/}userType" minOccurs="0"/>
 *         &lt;element name="formatComponentUserTypes" type="{http://impl.service.wfe.runa.ru/}userType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="publicAccess" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}anyType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "variableDefinition", propOrder = {
    "synthetic",
    "name",
    "scriptingName",
    "description",
    "format",
    "formatLabel",
    "userType",
    "formatComponentUserTypes",
    "publicAccess",
    "defaultValue"
})
public class VariableDefinition {

    protected boolean synthetic;
    protected String name;
    protected String scriptingName;
    protected String description;
    protected String format;
    protected String formatLabel;
    protected UserType userType;
    @XmlElement(nillable = true)
    protected List<UserType> formatComponentUserTypes;
    protected boolean publicAccess;
    protected Object defaultValue;

    /**
     * Gets the value of the synthetic property.
     * 
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Sets the value of the synthetic property.
     * 
     */
    public void setSynthetic(boolean value) {
        this.synthetic = value;
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
     * Gets the value of the scriptingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScriptingName() {
        return scriptingName;
    }

    /**
     * Sets the value of the scriptingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScriptingName(String value) {
        this.scriptingName = value;
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
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the formatLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormatLabel() {
        return formatLabel;
    }

    /**
     * Sets the value of the formatLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormatLabel(String value) {
        this.formatLabel = value;
    }

    /**
     * Gets the value of the userType property.
     * 
     * @return
     *     possible object is
     *     {@link UserType }
     *     
     */
    public UserType getUserType() {
        return userType;
    }

    /**
     * Sets the value of the userType property.
     * 
     * @param value
     *     allowed object is
     *     {@link UserType }
     *     
     */
    public void setUserType(UserType value) {
        this.userType = value;
    }

    /**
     * Gets the value of the formatComponentUserTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formatComponentUserTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormatComponentUserTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link UserType }
     * 
     * 
     */
    public List<UserType> getFormatComponentUserTypes() {
        if (formatComponentUserTypes == null) {
            formatComponentUserTypes = new ArrayList<UserType>();
        }
        return this.formatComponentUserTypes;
    }

    /**
     * Gets the value of the publicAccess property.
     * 
     */
    public boolean isPublicAccess() {
        return publicAccess;
    }

    /**
     * Sets the value of the publicAccess property.
     * 
     */
    public void setPublicAccess(boolean value) {
        this.publicAccess = value;
    }

    /**
     * Gets the value of the defaultValue property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the value of the defaultValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setDefaultValue(Object value) {
        this.defaultValue = value;
    }

}
