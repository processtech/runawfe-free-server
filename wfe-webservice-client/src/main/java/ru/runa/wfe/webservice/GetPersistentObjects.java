
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPersistentObjects complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getPersistentObjects">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="batchPresentation" type="{http://impl.service.wfe.runa.ru/}batchPresentation" minOccurs="0"/>
 *         &lt;element name="persistentClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="permission" type="{http://impl.service.wfe.runa.ru/}permission" minOccurs="0"/>
 *         &lt;element name="securedObjectTypes" type="{http://impl.service.wfe.runa.ru/}securedObjectType" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="enablePaging" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPersistentObjects", propOrder = {
    "user",
    "batchPresentation",
    "persistentClass",
    "permission",
    "securedObjectTypes",
    "enablePaging"
})
public class GetPersistentObjects {

    protected User user;
    protected BatchPresentation batchPresentation;
    protected String persistentClass;
    protected Permission permission;
    protected List<SecuredObjectType> securedObjectTypes;
    protected boolean enablePaging;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link User }
     *     
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link User }
     *     
     */
    public void setUser(User value) {
        this.user = value;
    }

    /**
     * Gets the value of the batchPresentation property.
     * 
     * @return
     *     possible object is
     *     {@link BatchPresentation }
     *     
     */
    public BatchPresentation getBatchPresentation() {
        return batchPresentation;
    }

    /**
     * Sets the value of the batchPresentation property.
     * 
     * @param value
     *     allowed object is
     *     {@link BatchPresentation }
     *     
     */
    public void setBatchPresentation(BatchPresentation value) {
        this.batchPresentation = value;
    }

    /**
     * Gets the value of the persistentClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPersistentClass() {
        return persistentClass;
    }

    /**
     * Sets the value of the persistentClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPersistentClass(String value) {
        this.persistentClass = value;
    }

    /**
     * Gets the value of the permission property.
     * 
     * @return
     *     possible object is
     *     {@link Permission }
     *     
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Sets the value of the permission property.
     * 
     * @param value
     *     allowed object is
     *     {@link Permission }
     *     
     */
    public void setPermission(Permission value) {
        this.permission = value;
    }

    /**
     * Gets the value of the securedObjectTypes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the securedObjectTypes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSecuredObjectTypes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SecuredObjectType }
     * 
     * 
     */
    public List<SecuredObjectType> getSecuredObjectTypes() {
        if (securedObjectTypes == null) {
            securedObjectTypes = new ArrayList<SecuredObjectType>();
        }
        return this.securedObjectTypes;
    }

    /**
     * Gets the value of the enablePaging property.
     * 
     */
    public boolean isEnablePaging() {
        return enablePaging;
    }

    /**
     * Sets the value of the enablePaging property.
     * 
     */
    public void setEnablePaging(boolean value) {
        this.enablePaging = value;
    }

}
