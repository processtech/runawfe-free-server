
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for isAllowed complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="isAllowed">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="permission" type="{http://impl.service.wfe.runa.ru/}permission" minOccurs="0"/>
 *         &lt;element name="identifiable" type="{http://impl.service.wfe.runa.ru/}identifiable" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "isAllowed", propOrder = {
    "user",
    "permission",
    "identifiable"
})
public class IsAllowed {

    protected User user;
    protected Permission permission;
    protected Identifiable identifiable;

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
     * Gets the value of the identifiable property.
     * 
     * @return
     *     possible object is
     *     {@link Identifiable }
     *     
     */
    public Identifiable getIdentifiable() {
        return identifiable;
    }

    /**
     * Sets the value of the identifiable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Identifiable }
     *     
     */
    public void setIdentifiable(Identifiable value) {
        this.identifiable = value;
    }

}
