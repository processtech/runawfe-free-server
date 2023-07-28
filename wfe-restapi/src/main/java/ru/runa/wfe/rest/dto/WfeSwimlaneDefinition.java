package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class WfeSwimlaneDefinition {
    private String name;
    private String orgFunctionLabel;
    private List<String> flowNodeIds;
    private String scriptingName;
    private boolean global;
}
