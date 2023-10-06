package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeVariableDefinition {
    private String name;
    private String scriptingName;
    private String description;
    private WfeVariableType type;
    private String format;
    private WfeVariableUserType userType;
    // for lists
    private WfeVariableUserType componentUserType;
    private WfeVariableType componentType;
}
