
package ru.runa.wfe.webservice;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for nodeType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="nodeType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="START_EVENT"/>
 *     &lt;enumeration value="ACTION_NODE"/>
 *     &lt;enumeration value="END_PROCESS"/>
 *     &lt;enumeration value="WAIT_STATE"/>
 *     &lt;enumeration value="TASK_STATE"/>
 *     &lt;enumeration value="FORK"/>
 *     &lt;enumeration value="JOIN"/>
 *     &lt;enumeration value="DECISION"/>
 *     &lt;enumeration value="SUBPROCESS"/>
 *     &lt;enumeration value="MULTI_SUBPROCESS"/>
 *     &lt;enumeration value="SEND_MESSAGE"/>
 *     &lt;enumeration value="RECEIVE_MESSAGE"/>
 *     &lt;enumeration value="END_TOKEN"/>
 *     &lt;enumeration value="MULTI_TASK_STATE"/>
 *     &lt;enumeration value="MERGE"/>
 *     &lt;enumeration value="EXCLUSIVE_GATEWAY"/>
 *     &lt;enumeration value="PARALLEL_GATEWAY"/>
 *     &lt;enumeration value="TEXT_ANNOTATION"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "nodeType")
@XmlEnum
public enum NodeType {

    START_EVENT,
    ACTION_NODE,
    END_PROCESS,
    WAIT_STATE,
    TASK_STATE,
    FORK,
    JOIN,
    DECISION,
    SUBPROCESS,
    MULTI_SUBPROCESS,
    SEND_MESSAGE,
    RECEIVE_MESSAGE,
    END_TOKEN,
    MULTI_TASK_STATE,
    MERGE,
    EXCLUSIVE_GATEWAY,
    PARALLEL_GATEWAY,
    TEXT_ANNOTATION;

    public String value() {
        return name();
    }

    public static NodeType fromValue(String v) {
        return valueOf(v);
    }

}
