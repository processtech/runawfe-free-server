package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeRelationPair {
    private Long id;
    private Long leftId;
    private Long rightId;
    private WfeRelation relation;
}
