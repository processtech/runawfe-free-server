package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.user.Group;

import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfGroupDtoMapper {

    WfGroupDto map(Group group);

    List<WfGroupDto> map(List<Group> groups);
}
