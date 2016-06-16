
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for createSubstitution complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="createSubstitution">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="substitution" type="{http://impl.service.wfe.runa.ru/}substitution" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createSubstitution", propOrder = {
    "user",
    "substitution"
})
public class CreateSubstitution {

    protected User user;
    protected Substitution substitution;

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
     * Gets the value of the substitution property.
     * 
     * @return
     *     possible object is
     *     {@link Substitution }
     *     
     */
    public Substitution getSubstitution() {
        return substitution;
    }

    /**
     * Sets the value of the substitution property.
     * 
     * @param value
     *     allowed object is
     *     {@link Substitution }
     *     
     */
    public void setSubstitution(Substitution value) {
        this.substitution = value;
    }

}
