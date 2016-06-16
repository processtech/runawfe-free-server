
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getExecutors complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getExecutors">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="batchPresentation" type="{http://impl.service.wfe.runa.ru/}batchPresentation" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getExecutors", propOrder = {
    "user",
    "batchPresentation"
})
public class GetExecutors {

    protected User user;
    protected BatchPresentation batchPresentation;

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

}
