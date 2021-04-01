package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.user.Actor;

public @Mapper(uses = WfVariableMapper.class)
interface WfUserDtoMapper {
    WfUserDto map(Actor actor);
}
