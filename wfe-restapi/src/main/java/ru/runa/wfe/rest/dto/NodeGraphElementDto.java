package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.lang.NodeType;

@Data
public class NodeGraphElementDto {
    private String nodeId;
    private NodeType nodeType;
    private String name;
    private int[] graphConstraints;
    private String label;
}
