
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for processDefinitionAccessType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="processDefinitionAccessType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Process"/>
 *     &lt;enumeration value="OnlySubprocess"/>
 *     &lt;enumeration value="EmbeddedSubprocess"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "processDefinitionAccessType")
@XmlEnum
public enum ProcessDefinitionAccessType {

    @XmlEnumValue("Process")
    PROCESS("Process"),
    @XmlEnumValue("OnlySubprocess")
    ONLY_SUBPROCESS("OnlySubprocess"),
    @XmlEnumValue("EmbeddedSubprocess")
    EMBEDDED_SUBPROCESS("EmbeddedSubprocess");
    private final String value;

    ProcessDefinitionAccessType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ProcessDefinitionAccessType fromValue(String v) {
        for (ProcessDefinitionAccessType c: ProcessDefinitionAccessType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
