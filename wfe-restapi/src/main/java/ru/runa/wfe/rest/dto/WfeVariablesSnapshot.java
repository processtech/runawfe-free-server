package ru.runa.wfe.rest.dto;

import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class WfeVariablesSnapshot {
    private List<WfeVariable> variables;
    private List<WfeVariable> startDateRangeVariables;
    private Set<String> simpleVariablesChanged;
}
