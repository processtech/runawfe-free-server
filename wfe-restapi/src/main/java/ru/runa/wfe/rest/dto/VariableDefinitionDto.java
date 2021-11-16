package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.var.VariableStoreType;

@Data
public class VariableDefinitionDto {
    private String name;
    private String scriptingName;
    private String description;
    private String formatLabel;
    private String userType;
    private Object defaultValue;
    private VariableStoreType storeType;
}
