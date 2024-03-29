package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;
import ru.runa.wfe.execution.ExecutionStatus;

@Data
public class WfeProcess {
    private Long id;
    private ExecutionStatus executionStatus;
    private Date startDate;
    private Date endDate; 
    private Long definitionId;
    private String definitionName;
    private int definitionVersion;
    private boolean archived;
    private List<WfeVariable> variables = new ArrayList<>();
}
