package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.user.Actor;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfUserDtoMapper {
    WfUserDto map(Actor actor);

    List<WfUserDto> map(List<Actor> actor);

    default Actor map(WfUserDto dto) {
        return new Actor(dto.getName(), dto.getDescription(), dto.getFullName(), dto.getCode(),
                dto.getEmail(), dto.getPhone(), dto.getTitle(), dto.getDepartment());
    }
}
