
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for authenticateByTrsustedPrincipal complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="authenticateByTrsustedPrincipal">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serviceUser" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="login" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "authenticateByTrsustedPrincipal", propOrder = {
    "serviceUser",
    "login"
})
public class AuthenticateByTrsustedPrincipal {

    protected User serviceUser;
    protected String login;

    /**
     * Gets the value of the serviceUser property.
     * 
     * @return
     *     possible object is
     *     {@link User }
     *     
     */
    public User getServiceUser() {
        return serviceUser;
    }

    /**
     * Sets the value of the serviceUser property.
     * 
     * @param value
     *     allowed object is
     *     {@link User }
     *     
     */
    public void setServiceUser(User value) {
        this.serviceUser = value;
    }

    /**
     * Gets the value of the login property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLogin() {
        return login;
    }

    /**
     * Sets the value of the login property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLogin(String value) {
        this.login = value;
    }

}
