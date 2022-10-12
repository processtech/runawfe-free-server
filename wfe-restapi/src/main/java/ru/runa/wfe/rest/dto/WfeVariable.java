package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeVariable {
    private String name;
    private Object value;
    private String format;

}
