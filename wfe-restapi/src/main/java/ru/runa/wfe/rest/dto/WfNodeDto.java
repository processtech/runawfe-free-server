package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.lang.NodeType;

@Data
public class WfNodeDto {
    private String parentId;
    private String id;
    private NodeType type;
    private String name;
    private String description;
    private boolean hasErrorEventHandler;
    private String swimlaneName;
}
