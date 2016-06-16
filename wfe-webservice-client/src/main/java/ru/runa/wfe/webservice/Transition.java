
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for transition complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="transition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}graphElement">
 *       &lt;sequence>
 *         &lt;element name="from" type="{http://impl.service.wfe.runa.ru/}node" minOccurs="0"/>
 *         &lt;element name="to" type="{http://impl.service.wfe.runa.ru/}node" minOccurs="0"/>
 *         &lt;element name="timerTransition" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="bendpoints" type="{http://impl.service.wfe.runa.ru/}bendpoint" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "transition", propOrder = {
    "from",
    "to",
    "timerTransition",
    "bendpoints"
})
public class Transition
    extends GraphElement
{

    protected Node from;
    protected Node to;
    protected boolean timerTransition;
    @XmlElement(nillable = true)
    protected List<Bendpoint> bendpoints;

    /**
     * Gets the value of the from property.
     * 
     * @return
     *     possible object is
     *     {@link Node }
     *     
     */
    public Node getFrom() {
        return from;
    }

    /**
     * Sets the value of the from property.
     * 
     * @param value
     *     allowed object is
     *     {@link Node }
     *     
     */
    public void setFrom(Node value) {
        this.from = value;
    }

    /**
     * Gets the value of the to property.
     * 
     * @return
     *     possible object is
     *     {@link Node }
     *     
     */
    public Node getTo() {
        return to;
    }

    /**
     * Sets the value of the to property.
     * 
     * @param value
     *     allowed object is
     *     {@link Node }
     *     
     */
    public void setTo(Node value) {
        this.to = value;
    }

    /**
     * Gets the value of the timerTransition property.
     * 
     */
    public boolean isTimerTransition() {
        return timerTransition;
    }

    /**
     * Sets the value of the timerTransition property.
     * 
     */
    public void setTimerTransition(boolean value) {
        this.timerTransition = value;
    }

    /**
     * Gets the value of the bendpoints property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bendpoints property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBendpoints().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Bendpoint }
     * 
     * 
     */
    public List<Bendpoint> getBendpoints() {
        if (bendpoints == null) {
            bendpoints = new ArrayList<Bendpoint>();
        }
        return this.bendpoints;
    }

}
