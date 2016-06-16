
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for subprocessDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="subprocessDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}processDefinition">
 *       &lt;sequence>
 *         &lt;element name="parentProcessDefinition" type="{http://impl.service.wfe.runa.ru/}processDefinition" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "subprocessDefinition", propOrder = {
    "parentProcessDefinition"
})
public class SubprocessDefinition
    extends ProcessDefinition
{

    protected ProcessDefinition parentProcessDefinition;

    /**
     * Gets the value of the parentProcessDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link ProcessDefinition }
     *     
     */
    public ProcessDefinition getParentProcessDefinition() {
        return parentProcessDefinition;
    }

    /**
     * Sets the value of the parentProcessDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProcessDefinition }
     *     
     */
    public void setParentProcessDefinition(ProcessDefinition value) {
        this.parentProcessDefinition = value;
    }

}
