package ru.runa.wfe.rest.dto;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class WfVariableHistoryStateDto {
    private List<WfVariableDto> variables;
    private List<WfVariableDto> startDateRangeVariables;
    private Set<String> simpleVariablesChanged;
}
