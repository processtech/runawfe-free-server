
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getSubstitutionsByCriteria complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSubstitutionsByCriteria">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="substitutionCriteria" type="{http://impl.service.wfe.runa.ru/}substitutionCriteria" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSubstitutionsByCriteria", propOrder = {
    "user",
    "substitutionCriteria"
})
public class GetSubstitutionsByCriteria {

    protected User user;
    protected SubstitutionCriteria substitutionCriteria;

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
     * Gets the value of the substitutionCriteria property.
     * 
     * @return
     *     possible object is
     *     {@link SubstitutionCriteria }
     *     
     */
    public SubstitutionCriteria getSubstitutionCriteria() {
        return substitutionCriteria;
    }

    /**
     * Sets the value of the substitutionCriteria property.
     * 
     * @param value
     *     allowed object is
     *     {@link SubstitutionCriteria }
     *     
     */
    public void setSubstitutionCriteria(SubstitutionCriteria value) {
        this.substitutionCriteria = value;
    }

}
