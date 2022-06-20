package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;
import ru.runa.wfe.lang.NodeType;

@Data
public class WfeNode {
    private String parentId;
    private String id;
    private NodeType type;
    private String name;
    private String description;
    private boolean hasErrorEventHandler;
    private String swimlaneName;
    private List<WfeTransition> arrivingTransitions;
    private List<WfeTransition> leavingTransitions;
    private List<WfeNode> boundaryEvents;

}
