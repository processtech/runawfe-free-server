
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for node complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="node">
 *   &lt;complexContent>
 *     &lt;extension base="{http://impl.service.wfe.runa.ru/}graphElement">
 *       &lt;sequence>
 *         &lt;element name="leavingTransitions" type="{http://impl.service.wfe.runa.ru/}transition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="arrivingTransitions" type="{http://impl.service.wfe.runa.ru/}transition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="graphMinimazedView" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="originalConstraints" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "node", propOrder = {
    "leavingTransitions",
    "arrivingTransitions",
    "graphMinimazedView",
    "originalConstraints"
})
@XmlSeeAlso({
    InteractionNode.class
})
public abstract class Node
    extends GraphElement
{

    @XmlElement(nillable = true)
    protected List<Transition> leavingTransitions;
    @XmlElement(nillable = true)
    protected List<Transition> arrivingTransitions;
    protected boolean graphMinimazedView;
    @XmlElement(nillable = true)
    protected List<Integer> originalConstraints;

    /**
     * Gets the value of the leavingTransitions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the leavingTransitions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLeavingTransitions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Transition }
     * 
     * 
     */
    public List<Transition> getLeavingTransitions() {
        if (leavingTransitions == null) {
            leavingTransitions = new ArrayList<Transition>();
        }
        return this.leavingTransitions;
    }

    /**
     * Gets the value of the arrivingTransitions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arrivingTransitions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArrivingTransitions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Transition }
     * 
     * 
     */
    public List<Transition> getArrivingTransitions() {
        if (arrivingTransitions == null) {
            arrivingTransitions = new ArrayList<Transition>();
        }
        return this.arrivingTransitions;
    }

    /**
     * Gets the value of the graphMinimazedView property.
     * 
     */
    public boolean isGraphMinimazedView() {
        return graphMinimazedView;
    }

    /**
     * Sets the value of the graphMinimazedView property.
     * 
     */
    public void setGraphMinimazedView(boolean value) {
        this.graphMinimazedView = value;
    }

    /**
     * Gets the value of the originalConstraints property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the originalConstraints property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOriginalConstraints().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     * 
     * 
     */
    public List<Integer> getOriginalConstraints() {
        if (originalConstraints == null) {
            originalConstraints = new ArrayList<Integer>();
        }
        return this.originalConstraints;
    }

}
