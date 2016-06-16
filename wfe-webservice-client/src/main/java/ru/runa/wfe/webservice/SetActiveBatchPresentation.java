
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for setActiveBatchPresentation complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="setActiveBatchPresentation">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="batchPresentationId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="newActiveBatchName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "setActiveBatchPresentation", propOrder = {
    "user",
    "batchPresentationId",
    "newActiveBatchName"
})
public class SetActiveBatchPresentation {

    protected User user;
    protected String batchPresentationId;
    protected String newActiveBatchName;

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
     * Gets the value of the batchPresentationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    /**
     * Sets the value of the batchPresentationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBatchPresentationId(String value) {
        this.batchPresentationId = value;
    }

    /**
     * Gets the value of the newActiveBatchName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNewActiveBatchName() {
        return newActiveBatchName;
    }

    /**
     * Sets the value of the newActiveBatchName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNewActiveBatchName(String value) {
        this.newActiveBatchName = value;
    }

}
