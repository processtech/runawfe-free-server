package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.user.Group;

import java.util.List;

@Data
public class WfProfileDto {
    private WfUserDto user;
    private List<String> relations;
    private List<String> groups;
}
