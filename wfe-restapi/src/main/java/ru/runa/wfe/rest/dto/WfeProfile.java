package ru.runa.wfe.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class WfeProfile {
    private WfeUser user;
    private List<WfeRelation> relations;
    private List<WfeGroup> groups;
}
