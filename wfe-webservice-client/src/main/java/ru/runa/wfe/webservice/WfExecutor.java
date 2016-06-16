
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for wfExecutor complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="wfExecutor">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}executor">
 *       &lt;sequence>
 *         &lt;element name="executorClassName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wfExecutor", propOrder = {
    "executorClassName"
})
public class WfExecutor
    extends Executor
{

    protected String executorClassName;

    /**
     * Gets the value of the executorClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExecutorClassName() {
        return executorClassName;
    }

    /**
     * Sets the value of the executorClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExecutorClassName(String value) {
        this.executorClassName = value;
    }

}
