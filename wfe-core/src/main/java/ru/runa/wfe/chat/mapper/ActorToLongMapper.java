package ru.runa.wfe.chat.mapper;

import ru.runa.wfe.user.Actor;

public class ActorToLongMapper extends AbstractModelMapper<Actor, Long> {
    @Override
    public Long toDto(Actor entity) {
        return entity.getId();
    }
}
