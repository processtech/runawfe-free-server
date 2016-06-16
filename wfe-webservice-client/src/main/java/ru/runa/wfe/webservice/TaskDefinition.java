
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for taskDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="taskDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}graphElement">
 *       &lt;sequence>
 *         &lt;element name="deadlineDuration" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="node" type="{http://impl.service.wfe.runa.ru/}interactionNode" minOccurs="0"/>
 *         &lt;element name="swimlaneDefinition" type="{http://impl.service.wfe.runa.ru/}swimlaneDefinition" minOccurs="0"/>
 *         &lt;element name="reassignSwimlane" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="reassignSwimlaneToTaskPerformer" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="ignoreSubsitutionRules" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "taskDefinition", propOrder = {
    "deadlineDuration",
    "node",
    "swimlaneDefinition",
    "reassignSwimlane",
    "reassignSwimlaneToTaskPerformer",
    "ignoreSubsitutionRules"
})
public class TaskDefinition
    extends GraphElement
{

    protected String deadlineDuration;
    protected InteractionNode node;
    protected SwimlaneDefinition swimlaneDefinition;
    protected boolean reassignSwimlane;
    protected boolean reassignSwimlaneToTaskPerformer;
    protected boolean ignoreSubsitutionRules;

    /**
     * Gets the value of the deadlineDuration property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeadlineDuration() {
        return deadlineDuration;
    }

    /**
     * Sets the value of the deadlineDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeadlineDuration(String value) {
        this.deadlineDuration = value;
    }

    /**
     * Gets the value of the node property.
     * 
     * @return
     *     possible object is
     *     {@link InteractionNode }
     *     
     */
    public InteractionNode getNode() {
        return node;
    }

    /**
     * Sets the value of the node property.
     * 
     * @param value
     *     allowed object is
     *     {@link InteractionNode }
     *     
     */
    public void setNode(InteractionNode value) {
        this.node = value;
    }

    /**
     * Gets the value of the swimlaneDefinition property.
     * 
     * @return
     *     possible object is
     *     {@link SwimlaneDefinition }
     *     
     */
    public SwimlaneDefinition getSwimlaneDefinition() {
        return swimlaneDefinition;
    }

    /**
     * Sets the value of the swimlaneDefinition property.
     * 
     * @param value
     *     allowed object is
     *     {@link SwimlaneDefinition }
     *     
     */
    public void setSwimlaneDefinition(SwimlaneDefinition value) {
        this.swimlaneDefinition = value;
    }

    /**
     * Gets the value of the reassignSwimlane property.
     * 
     */
    public boolean isReassignSwimlane() {
        return reassignSwimlane;
    }

    /**
     * Sets the value of the reassignSwimlane property.
     * 
     */
    public void setReassignSwimlane(boolean value) {
        this.reassignSwimlane = value;
    }

    /**
     * Gets the value of the reassignSwimlaneToTaskPerformer property.
     * 
     */
    public boolean isReassignSwimlaneToTaskPerformer() {
        return reassignSwimlaneToTaskPerformer;
    }

    /**
     * Sets the value of the reassignSwimlaneToTaskPerformer property.
     * 
     */
    public void setReassignSwimlaneToTaskPerformer(boolean value) {
        this.reassignSwimlaneToTaskPerformer = value;
    }

    /**
     * Gets the value of the ignoreSubsitutionRules property.
     * 
     */
    public boolean isIgnoreSubsitutionRules() {
        return ignoreSubsitutionRules;
    }

    /**
     * Sets the value of the ignoreSubsitutionRules property.
     * 
     */
    public void setIgnoreSubsitutionRules(boolean value) {
        this.ignoreSubsitutionRules = value;
    }

}
