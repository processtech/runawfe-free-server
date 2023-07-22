package ru.runa.wfe.var.dto;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class Variables {
    public static final String CURRENT_PROCESS_ID = "currentProcessId";
    public static final String CURRENT_PROCESS_DEFINITION_NAME = "currentDefinitionName";
    public static final String CURRENT_NODE_NAME = "currentNodeName";
    public static final String CURRENT_NODE_ID = "currentNodeId";
    public static final String CURRENT_PROCESS_ID_WRAPPED = wrap(CURRENT_PROCESS_ID);
    public static final String CURRENT_PROCESS_DEFINITION_NAME_WRAPPED = wrap(CURRENT_PROCESS_DEFINITION_NAME);
    public static final String CURRENT_NODE_NAME_WRAPPED = wrap(CURRENT_NODE_NAME);
    public static final String CURRENT_NODE_ID_WRAPPED = wrap(CURRENT_NODE_ID);

    public static String wrap(String variableName) {
        return "${" + variableName + "}";
    }

    /**
     * Convert variables list to map from variable name to variable.
     *
     * @param variables
     *            Variables list.
     * @return map from variable name to variable.
     */
    public static Map<String, WfVariable> toMap(List<WfVariable> variables) {
        Map<String, WfVariable> map = Maps.newHashMap();
        for (WfVariable variable : variables) {
            map.put(variable.getDefinition().getName(), variable);
        }
        return map;
    }

}
