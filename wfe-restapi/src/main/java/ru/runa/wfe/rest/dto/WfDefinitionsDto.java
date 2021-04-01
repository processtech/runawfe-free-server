package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class WfDefinitionsDto {
    private Integer total;
    private List<WfDefinitionDto> definitions;
}
