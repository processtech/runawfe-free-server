
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for securedObjectType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="securedObjectType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NONE"/>
 *     &lt;enumeration value="SYSTEM"/>
 *     &lt;enumeration value="BOTSTATION"/>
 *     &lt;enumeration value="ACTOR"/>
 *     &lt;enumeration value="GROUP"/>
 *     &lt;enumeration value="RELATION"/>
 *     &lt;enumeration value="RELATIONGROUP"/>
 *     &lt;enumeration value="RELATIONPAIR"/>
 *     &lt;enumeration value="DEFINITION"/>
 *     &lt;enumeration value="PROCESS"/>
 *     &lt;enumeration value="REPORT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "securedObjectType")
@XmlEnum
public enum SecuredObjectType {

    NONE,
    SYSTEM,
    BOTSTATION,
    ACTOR,
    GROUP,
    RELATION,
    RELATIONGROUP,
    RELATIONPAIR,
    DEFINITION,
    PROCESS,
    REPORT;

    public String value() {
        return name();
    }

    public static SecuredObjectType fromValue(String v) {
        return valueOf(v);
    }

}
