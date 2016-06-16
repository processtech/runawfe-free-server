
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getIssuedPermissions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getIssuedPermissions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="performer" type="{http://impl.service.wfe.runa.ru/}wfExecutor" minOccurs="0"/>
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
@XmlType(name = "getIssuedPermissions", propOrder = {
    "user",
    "performer",
    "identifiable"
})
public class GetIssuedPermissions {

    protected User user;
    protected WfExecutor performer;
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
     * Gets the value of the performer property.
     * 
     * @return
     *     possible object is
     *     {@link WfExecutor }
     *     
     */
    public WfExecutor getPerformer() {
        return performer;
    }

    /**
     * Sets the value of the performer property.
     * 
     * @param value
     *     allowed object is
     *     {@link WfExecutor }
     *     
     */
    public void setPerformer(WfExecutor value) {
        this.performer = value;
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
