package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class WfeTask {
    private Long id;
    private String name;
    private String nodeId;
    private String description;
    private String swimlaneName;
    private WfeExecutor owner;
    private WfeUser targetUser;
    private Long definitionId;
    private String definitionName;
    private Long processId;
    private String processHierarchyIds;
    private Long tokenId;
    private Date createDate;
    private Date deadlineDate;
    private Date deadlineWarningDate;
    private Date assignDate;
    private boolean escalated;
    private boolean firstOpen;
    private boolean acquiredBySubstitution;
    private Integer multitaskIndex;
    private boolean readOnly;
    private List<WfeVariable> variables = new ArrayList<>();
}
