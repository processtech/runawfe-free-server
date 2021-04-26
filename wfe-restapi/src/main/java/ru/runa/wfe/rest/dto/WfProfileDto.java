package ru.runa.wfe.rest.dto;

import lombok.Data;

import java.util.List;

@Data
public class WfProfileDto {
    private WfUserDto user;
    private List<WfRelationDto> relations;
    private List<WfGroupDto> groups;
}
