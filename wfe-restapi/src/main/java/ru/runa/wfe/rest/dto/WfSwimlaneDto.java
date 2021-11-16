package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.user.Executor;

@Data
public class WfSwimlaneDto {
    private Long id;
    private SwimlaneDefinitionDto definition;
    private Executor executor;
}
