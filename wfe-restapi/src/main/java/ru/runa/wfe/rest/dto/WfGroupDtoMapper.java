package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.user.Group;

import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfGroupDtoMapper {

    WfGroupDto map(Group group);

    List<WfGroupDto> map(List<Group> groups);

    default Group map(WfGroupDto dto) {
        Group group = new Group(dto.getName(), dto.getDescription());
        group.setId(dto.getId());
        group.setFullName(dto.getFullName());
        return group;
    }
}
