
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for wfTask complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="wfTask">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nodeId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="swimlaneName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="owner" type="{http://impl.service.wfe.runa.ru/}wfExecutor" minOccurs="0"/>
 *         &lt;element name="targetActor" type="{http://impl.service.wfe.runa.ru/}actor" minOccurs="0"/>
 *         &lt;element name="definitionId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="definitionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="processId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="processHierarchyIds" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="creationDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="deadlineDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="deadlineWarningDate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="escalated" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="firstOpen" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="acquiredBySubstitution" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="multitaskIndex" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="variables" type="{http://stub.service.wfe.runa.ru/}wfVariableStub" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "wfTask", propOrder = {
    "id",
    "name",
    "nodeId",
    "description",
    "swimlaneName",
    "owner",
    "targetActor",
    "definitionId",
    "definitionName",
    "processId",
    "processHierarchyIds",
    "creationDate",
    "deadlineDate",
    "deadlineWarningDate",
    "escalated",
    "firstOpen",
    "acquiredBySubstitution",
    "multitaskIndex",
    "variables"
})
public class WfTask {

    protected Long id;
    protected String name;
    protected String nodeId;
    protected String description;
    protected String swimlaneName;
    protected WfExecutor owner;
    protected Actor targetActor;
    protected Long definitionId;
    protected String definitionName;
    protected Long processId;
    protected String processHierarchyIds;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar creationDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar deadlineDate;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar deadlineWarningDate;
    protected boolean escalated;
    protected boolean firstOpen;
    protected boolean acquiredBySubstitution;
    protected Integer multitaskIndex;
    @XmlElement(nillable = true)
    protected List<WfVariableStub> variables;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setId(Long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the nodeId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNodeId() {
        return nodeId;
    }

    /**
     * Sets the value of the nodeId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNodeId(String value) {
        this.nodeId = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the swimlaneName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSwimlaneName() {
        return swimlaneName;
    }

    /**
     * Sets the value of the swimlaneName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSwimlaneName(String value) {
        this.swimlaneName = value;
    }

    /**
     * Gets the value of the owner property.
     * 
     * @return
     *     possible object is
     *     {@link WfExecutor }
     *     
     */
    public WfExecutor getOwner() {
        return owner;
    }

    /**
     * Sets the value of the owner property.
     * 
     * @param value
     *     allowed object is
     *     {@link WfExecutor }
     *     
     */
    public void setOwner(WfExecutor value) {
        this.owner = value;
    }

    /**
     * Gets the value of the targetActor property.
     * 
     * @return
     *     possible object is
     *     {@link Actor }
     *     
     */
    public Actor getTargetActor() {
        return targetActor;
    }

    /**
     * Sets the value of the targetActor property.
     * 
     * @param value
     *     allowed object is
     *     {@link Actor }
     *     
     */
    public void setTargetActor(Actor value) {
        this.targetActor = value;
    }

    /**
     * Gets the value of the definitionId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDefinitionId() {
        return definitionId;
    }

    /**
     * Sets the value of the definitionId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDefinitionId(Long value) {
        this.definitionId = value;
    }

    /**
     * Gets the value of the definitionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefinitionName() {
        return definitionName;
    }

    /**
     * Sets the value of the definitionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefinitionName(String value) {
        this.definitionName = value;
    }

    /**
     * Gets the value of the processId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getProcessId() {
        return processId;
    }

    /**
     * Sets the value of the processId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setProcessId(Long value) {
        this.processId = value;
    }

    /**
     * Gets the value of the processHierarchyIds property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getProcessHierarchyIds() {
        return processHierarchyIds;
    }

    /**
     * Sets the value of the processHierarchyIds property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setProcessHierarchyIds(String value) {
        this.processHierarchyIds = value;
    }

    /**
     * Gets the value of the creationDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the value of the creationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setCreationDate(XMLGregorianCalendar value) {
        this.creationDate = value;
    }

    /**
     * Gets the value of the deadlineDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDeadlineDate() {
        return deadlineDate;
    }

    /**
     * Sets the value of the deadlineDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDeadlineDate(XMLGregorianCalendar value) {
        this.deadlineDate = value;
    }

    /**
     * Gets the value of the deadlineWarningDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getDeadlineWarningDate() {
        return deadlineWarningDate;
    }

    /**
     * Sets the value of the deadlineWarningDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setDeadlineWarningDate(XMLGregorianCalendar value) {
        this.deadlineWarningDate = value;
    }

    /**
     * Gets the value of the escalated property.
     * 
     */
    public boolean isEscalated() {
        return escalated;
    }

    /**
     * Sets the value of the escalated property.
     * 
     */
    public void setEscalated(boolean value) {
        this.escalated = value;
    }

    /**
     * Gets the value of the firstOpen property.
     * 
     */
    public boolean isFirstOpen() {
        return firstOpen;
    }

    /**
     * Sets the value of the firstOpen property.
     * 
     */
    public void setFirstOpen(boolean value) {
        this.firstOpen = value;
    }

    /**
     * Gets the value of the acquiredBySubstitution property.
     * 
     */
    public boolean isAcquiredBySubstitution() {
        return acquiredBySubstitution;
    }

    /**
     * Sets the value of the acquiredBySubstitution property.
     * 
     */
    public void setAcquiredBySubstitution(boolean value) {
        this.acquiredBySubstitution = value;
    }

    /**
     * Gets the value of the multitaskIndex property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getMultitaskIndex() {
        return multitaskIndex;
    }

    /**
     * Sets the value of the multitaskIndex property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setMultitaskIndex(Integer value) {
        this.multitaskIndex = value;
    }

    /**
     * Gets the value of the variables property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the variables property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVariables().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link WfVariableStub }
     * 
     * 
     */
    public List<WfVariableStub> getVariables() {
        if (variables == null) {
            variables = new ArrayList<WfVariableStub>();
        }
        return this.variables;
    }

}
