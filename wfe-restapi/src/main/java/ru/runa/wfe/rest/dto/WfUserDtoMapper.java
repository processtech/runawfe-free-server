package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.user.Actor;

@Mapper(uses = WfVariableMapper.class)
public interface WfUserDtoMapper {
    WfUserDto map(Actor actor);
}
