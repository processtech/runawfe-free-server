package ru.runa.wfe.rest.dto;

import java.util.List;
import java.util.Map;
import lombok.Data;
import ru.runa.wfe.lang.NodeType;

@Data
public class WfeNodeGraphElement {
    private String nodeId;
    private NodeType nodeType;
    private String name;
    private int[] graphConstraints;
    private String label;
    private List<WfeProcessLog> data;
    private Map<String, Object> additionalProperties;

}
