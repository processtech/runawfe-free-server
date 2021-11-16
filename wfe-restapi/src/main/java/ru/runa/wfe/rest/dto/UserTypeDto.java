package ru.runa.wfe.rest.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserTypeDto {
    private String name;
    private List<VariableDefinitionDto> attributes;
}
