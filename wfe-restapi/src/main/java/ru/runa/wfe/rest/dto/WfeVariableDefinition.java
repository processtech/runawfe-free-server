package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.var.VariableStoreType;

@Data
public class WfeVariableDefinition {
    private String name;
    private String scriptingName;
    private String description;
    private String format;
    private String formatLabel;
    private String formatClassName;
    private WfeVariableUserType userType;
    private WfeVariableUserType[] formatComponentUserTypes;
    private String[] formatComponentClassNames;
    private Object defaultValue;
    private VariableStoreType storeType;
    private boolean global;
}
