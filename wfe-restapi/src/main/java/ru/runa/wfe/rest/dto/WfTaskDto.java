package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class WfTaskDto {
    private Long id;
    private String name;
    private String category;
    private String nodeId;
    private String description;
    private String swimlaneName;
    private ExecutorDto owner;
    private ExecutorDto targetActor;
    private Long definitionVersionId;
    private String definitionName;
    private Long processId;
    private String processHierarchyIds;
    private Long tokenId;
    private Date creationDate;
    private Date deadlineDate;
    private Date deadlineWarningDate;
    private Date assignDate;
    private boolean escalated;
    private boolean firstOpen;
    private boolean acquiredBySubstitution;
    private Integer multitaskIndex;
    private boolean readOnly;
    private List<WfVariableDto> variables = new ArrayList<>();
}
