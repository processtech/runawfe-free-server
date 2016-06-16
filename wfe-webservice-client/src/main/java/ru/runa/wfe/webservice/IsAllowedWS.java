
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for isAllowedWS complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="isAllowedWS">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="permission" type="{http://impl.service.wfe.runa.ru/}permission" minOccurs="0"/>
 *         &lt;element name="securedObjectType" type="{http://impl.service.wfe.runa.ru/}securedObjectType" minOccurs="0"/>
 *         &lt;element name="identifiableId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "isAllowedWS", propOrder = {
    "user",
    "permission",
    "securedObjectType",
    "identifiableId"
})
public class IsAllowedWS {

    protected User user;
    protected Permission permission;
    protected SecuredObjectType securedObjectType;
    protected Long identifiableId;

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
     * Gets the value of the securedObjectType property.
     * 
     * @return
     *     possible object is
     *     {@link SecuredObjectType }
     *     
     */
    public SecuredObjectType getSecuredObjectType() {
        return securedObjectType;
    }

    /**
     * Sets the value of the securedObjectType property.
     * 
     * @param value
     *     allowed object is
     *     {@link SecuredObjectType }
     *     
     */
    public void setSecuredObjectType(SecuredObjectType value) {
        this.securedObjectType = value;
    }

    /**
     * Gets the value of the identifiableId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getIdentifiableId() {
        return identifiableId;
    }

    /**
     * Sets the value of the identifiableId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setIdentifiableId(Long value) {
        this.identifiableId = value;
    }

}
