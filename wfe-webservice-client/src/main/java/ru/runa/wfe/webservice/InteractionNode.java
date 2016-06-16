
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for interactionNode complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="interactionNode">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}node">
 *       &lt;sequence>
 *         &lt;element name="taskDefinitions" type="{http://impl.service.wfe.runa.ru/}taskDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "interactionNode", propOrder = {
    "taskDefinitions"
})
@XmlSeeAlso({
    StartState.class
})
public abstract class InteractionNode
    extends Node
{

    @XmlElement(nillable = true)
    protected List<TaskDefinition> taskDefinitions;

    /**
     * Gets the value of the taskDefinitions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the taskDefinitions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTaskDefinitions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TaskDefinition }
     * 
     * 
     */
    public List<TaskDefinition> getTaskDefinitions() {
        if (taskDefinitions == null) {
            taskDefinitions = new ArrayList<TaskDefinition>();
        }
        return this.taskDefinitions;
    }

}
