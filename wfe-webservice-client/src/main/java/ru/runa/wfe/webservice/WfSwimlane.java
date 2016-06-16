
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for wfSwimlane complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="wfSwimlane">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="definition" type="{http://impl.service.wfe.runa.ru/}swimlaneDefinition" minOccurs="0"/>
 *         &lt;element name="executor" type="{http://impl.service.wfe.runa.ru/}wfExecutor" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wfSwimlane", propOrder = {
    "definition",
    "executor"
})
public class WfSwimlane {

    protected SwimlaneDefinition definition;
    protected WfExecutor executor;

    /**
     * Gets the value of the definition property.
     * 
     * @return
     *     possible object is
     *     {@link SwimlaneDefinition }
     *     
     */
    public SwimlaneDefinition getDefinition() {
        return definition;
    }

    /**
     * Sets the value of the definition property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwimlaneDefinition }
     *     
     */
    public void setDefinition(SwimlaneDefinition value) {
        this.definition = value;
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

}
