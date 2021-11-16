package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class InteractionDto {
    private String nodeId;
    private String name;
    private String description;
    private String type;
}
