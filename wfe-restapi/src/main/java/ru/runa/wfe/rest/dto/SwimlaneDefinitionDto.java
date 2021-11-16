package ru.runa.wfe.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class SwimlaneDefinitionDto {
    private String orgFunctionLabel;
    private List<String> flowNodeIds;
    private String scriptingName;
    private boolean global;
}
