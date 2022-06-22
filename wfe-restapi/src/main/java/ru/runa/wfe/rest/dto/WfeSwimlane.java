package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeSwimlane {
    private Long id;
    private WfeSwimlaneDefinition definition;
    private WfeExecutor executor;
}
