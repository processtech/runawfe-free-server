package ru.runa.wfe.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class WfeVariableUserType {
    private String name;
    private List<WfeVariableDefinition> attributes;
}
