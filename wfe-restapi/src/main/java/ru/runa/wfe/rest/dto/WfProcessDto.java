package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class WfProcessDto {
    private Long id;
    private String definitionName;
    private String executionStatus;
    private Date startDate;
    private Date endDate; 
    private List<WfVariableDto> variables = new ArrayList<>();
}
