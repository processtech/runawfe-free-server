package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class RelationPairDto {
    private Long id;
    private Long leftId;
    private Long rightId;
    private WfRelationDto relation;
}
