
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for importBot complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="importBot">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="user" type="{http://impl.service.wfe.runa.ru/}user" minOccurs="0"/>
 *         &lt;element name="botStation" type="{http://impl.service.wfe.runa.ru/}botStation" minOccurs="0"/>
 *         &lt;element name="archive" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="replace" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "importBot", propOrder = {
    "user",
    "botStation",
    "archive",
    "replace"
})
public class ImportBot {

    protected User user;
    protected BotStation botStation;
    protected byte[] archive;
    protected boolean replace;

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
     * Gets the value of the botStation property.
     * 
     * @return
     *     possible object is
     *     {@link BotStation }
     *     
     */
    public BotStation getBotStation() {
        return botStation;
    }

    /**
     * Sets the value of the botStation property.
     * 
     * @param value
     *     allowed object is
     *     {@link BotStation }
     *     
     */
    public void setBotStation(BotStation value) {
        this.botStation = value;
    }

    /**
     * Gets the value of the archive property.
     * 
     * @return
     *     possible object is
     *     byte[]
     */
    public byte[] getArchive() {
        return archive;
    }

    /**
     * Sets the value of the archive property.
     * 
     * @param value
     *     allowed object is
     *     byte[]
     */
    public void setArchive(byte[] value) {
        this.archive = ((byte[]) value);
    }

    /**
     * Gets the value of the replace property.
     * 
     */
    public boolean isReplace() {
        return replace;
    }

    /**
     * Sets the value of the replace property.
     * 
     */
    public void setReplace(boolean value) {
        this.replace = value;
    }

}
