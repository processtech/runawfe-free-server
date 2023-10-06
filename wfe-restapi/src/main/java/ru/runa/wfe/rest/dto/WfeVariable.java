package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeVariable {
    private String name;
    private WfeVariableType type;
    private String format;
    private Object value;
}
