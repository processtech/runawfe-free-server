
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for group complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="group">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}executor">
 *       &lt;sequence>
 *         &lt;element name="ldapGroupName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "group", propOrder = {
    "ldapGroupName"
})
public class Group
    extends Executor
{

    protected String ldapGroupName;

    /**
     * Gets the value of the ldapGroupName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLdapGroupName() {
        return ldapGroupName;
    }

    /**
     * Sets the value of the ldapGroupName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLdapGroupName(String value) {
        this.ldapGroupName = value;
    }

}
