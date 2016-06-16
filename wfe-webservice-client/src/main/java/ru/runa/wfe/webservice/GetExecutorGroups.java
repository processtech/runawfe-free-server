
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getExecutorGroups complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getExecutorGroups">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="executor" type="{http://impl.service.wfe.runa.ru/}wfExecutor" minOccurs="0"/>
 *         &lt;element name="batchPresentation" type="{http://impl.service.wfe.runa.ru/}batchPresentation" minOccurs="0"/>
 *         &lt;element name="excluded" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getExecutorGroups", propOrder = {
    "user",
    "executor",
    "batchPresentation",
    "excluded"
})
public class GetExecutorGroups {

    protected User user;
    protected WfExecutor executor;
    protected BatchPresentation batchPresentation;
    protected boolean excluded;

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
     * Gets the value of the executor property.
     * 
     * @return
     *     possible object is
     *     {@link WfExecutor }
     *     
     */
    public WfExecutor getExecutor() {
        return executor;
    }

    /**
     * Sets the value of the executor property.
     * 
     * @param value
     *     allowed object is
     *     {@link WfExecutor }
     *     
     */
    public void setExecutor(WfExecutor value) {
        this.executor = value;
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
     * Gets the value of the excluded property.
     * 
     */
    public boolean isExcluded() {
        return excluded;
    }

    /**
     * Sets the value of the excluded property.
     * 
     */
    public void setExcluded(boolean value) {
        this.excluded = value;
    }

}
