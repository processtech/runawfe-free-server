
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for swimlaneDefinition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="swimlaneDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}graphElement">
 *       &lt;sequence>
 *         &lt;element name="delegation" type="{http://impl.service.wfe.runa.ru/}delegation" minOccurs="0"/>
 *         &lt;element name="orgFunctionLabel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="flowNodeIds" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="scriptingName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "swimlaneDefinition", propOrder = {
    "delegation",
    "orgFunctionLabel",
    "flowNodeIds",
    "scriptingName"
})
public class SwimlaneDefinition
    extends GraphElement
{

    protected Delegation delegation;
    protected String orgFunctionLabel;
    @XmlElement(nillable = true)
    protected List<String> flowNodeIds;
    protected String scriptingName;

    /**
     * Gets the value of the delegation property.
     * 
     * @return
     *     possible object is
     *     {@link Delegation }
     *     
     */
    public Delegation getDelegation() {
        return delegation;
    }

    /**
     * Sets the value of the delegation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Delegation }
     *     
     */
    public void setDelegation(Delegation value) {
        this.delegation = value;
    }

    /**
     * Gets the value of the orgFunctionLabel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrgFunctionLabel() {
        return orgFunctionLabel;
    }

    /**
     * Sets the value of the orgFunctionLabel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrgFunctionLabel(String value) {
        this.orgFunctionLabel = value;
    }

    /**
     * Gets the value of the flowNodeIds property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flowNodeIds property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlowNodeIds().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getFlowNodeIds() {
        if (flowNodeIds == null) {
            flowNodeIds = new ArrayList<String>();
        }
        return this.flowNodeIds;
    }

    /**
     * Gets the value of the scriptingName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getScriptingName() {
        return scriptingName;
    }

    /**
     * Sets the value of the scriptingName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setScriptingName(String value) {
        this.scriptingName = value;
    }

}
