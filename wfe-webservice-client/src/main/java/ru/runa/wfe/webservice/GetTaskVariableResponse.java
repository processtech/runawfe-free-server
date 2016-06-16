
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getTaskVariableResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getTaskVariableResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="result" type="{http://stub.service.wfe.runa.ru/}wfVariableStub" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getTaskVariableResponse", propOrder = {
    "result"
})
public class GetTaskVariableResponse {

    protected WfVariableStub result;

    /**
     * Gets the value of the result property.
     * 
     * @return
     *     possible object is
     *     {@link WfVariableStub }
     *     
     */
    public WfVariableStub getResult() {
        return result;
    }

    /**
     * Sets the value of the result property.
     * 
     * @param value
     *     allowed object is
     *     {@link WfVariableStub }
     *     
     */
    public void setResult(WfVariableStub value) {
        this.result = value;
    }

}
