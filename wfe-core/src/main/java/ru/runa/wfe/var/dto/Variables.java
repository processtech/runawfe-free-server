package ru.runa.wfe.var.dto;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

public class Variables {
    public static final String CURRENT_PROCESS_ID = "currentProcessId";
    public static final String CURRENT_PROCESS_DEFINITION_NAME = "currentDefinitionName";
    public static final String CURRENT_NODE_NAME = "currentNodeName";
    public static final String CURRENT_NODE_ID = "currentNodeId";
    public static final String CURRENT_PROCESS_ID_WRAPPED = "${" + CURRENT_PROCESS_ID + "}";
    public static final String CURRENT_PROCESS_DEFINITION_NAME_WRAPPED = "${" + CURRENT_PROCESS_DEFINITION_NAME + "}";
    public static final String CURRENT_NODE_NAME_WRAPPED = "${" + CURRENT_NODE_NAME + "}";
    public static final String CURRENT_NODE_ID_WRAPPED = "${" + CURRENT_NODE_ID + "}";

    public static Map<String, Object> toMap(List<WfVariable> variables) {
        Map<String, Object> map = Maps.newHashMap();
        for (WfVariable variable : variables) {
            map.put(variable.getDefinition().getName(), variable.getValue());
        }
        return map;
    }
}
