package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.execution.ExecutionStatus;
import java.util.Date;

@Data
public class WfeToken {
    private Long parentId;
    private Long id;
    private Long processId;
    private String name;
    private Date startDate;
    private Date endDate;
    private WfeNode node;
    private String transitionId;
    private ExecutionStatus executionStatus;
    private Date errorDate;
    private String errorMessage;
}
