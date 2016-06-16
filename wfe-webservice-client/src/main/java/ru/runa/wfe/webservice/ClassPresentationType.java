
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for classPresentationType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="classPresentationType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="NONE"/>
 *     &lt;enumeration value="SYSTEM_LOG"/>
 *     &lt;enumeration value="EXECUTOR"/>
 *     &lt;enumeration value="ACTOR"/>
 *     &lt;enumeration value="GROUP"/>
 *     &lt;enumeration value="RELATION"/>
 *     &lt;enumeration value="RELATIONPAIR"/>
 *     &lt;enumeration value="DEFINITION"/>
 *     &lt;enumeration value="DEFINITION_HISTORY"/>
 *     &lt;enumeration value="PROCESS"/>
 *     &lt;enumeration value="TASK"/>
 *     &lt;enumeration value="REPORTS"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "classPresentationType")
@XmlEnum
public enum ClassPresentationType {

    NONE,
    SYSTEM_LOG,
    EXECUTOR,
    ACTOR,
    GROUP,
    RELATION,
    RELATIONPAIR,
    DEFINITION,
    DEFINITION_HISTORY,
    PROCESS,
    TASK,
    REPORTS;

    public String value() {
        return name();
    }

    public static ClassPresentationType fromValue(String v) {
        return valueOf(v);
    }

}
