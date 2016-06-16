
package ru.runa.wfe.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for setPermissions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="setPermissions">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="executorId" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="permissions" type="{http://impl.service.wfe.runa.ru/}permission" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="identifiable" type="{http://impl.service.wfe.runa.ru/}identifiable" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "setPermissions", propOrder = {
    "user",
    "executorId",
    "permissions",
    "identifiable"
})
public class SetPermissions {

    protected User user;
    protected Long executorId;
    protected List<Permission> permissions;
    protected Identifiable identifiable;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link User }
     *     
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link User }
     *     
     */
    public void setUser(User value) {
        this.user = value;
    }

    /**
     * Gets the value of the executorId property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExecutorId() {
        return executorId;
    }

    /**
     * Sets the value of the executorId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExecutorId(Long value) {
        this.executorId = value;
    }

    /**
     * Gets the value of the permissions property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the permissions property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPermissions().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Permission }
     * 
     * 
     */
    public List<Permission> getPermissions() {
        if (permissions == null) {
            permissions = new ArrayList<Permission>();
        }
        return this.permissions;
    }

    /**
     * Gets the value of the identifiable property.
     * 
     * @return
     *     possible object is
     *     {@link Identifiable }
     *     
     */
    public Identifiable getIdentifiable() {
        return identifiable;
    }

    /**
     * Sets the value of the identifiable property.
     * 
     * @param value
     *     allowed object is
     *     {@link Identifiable }
     *     
     */
    public void setIdentifiable(Identifiable value) {
        this.identifiable = value;
    }

}
