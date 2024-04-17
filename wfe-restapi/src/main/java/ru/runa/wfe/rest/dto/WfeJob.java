package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;
import ru.runa.wfe.lang.NodeType;

@Data
public class WfeJob {
    private Long id;
    private String name;
    private Long processId;
    private Long tokenId;
    private NodeType nodeType;
    private String nodeId;
    private Date createDate;
    private Date dueDate;
    private String dueDateExpression;

}
