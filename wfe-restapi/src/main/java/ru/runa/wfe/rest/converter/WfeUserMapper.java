package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.rest.dto.WfeUser;
import ru.runa.wfe.user.Actor;

@Mapper
public interface WfeUserMapper {

    WfeUser map(Actor actor);

    List<WfeUser> map(List<Actor> actors);

    @AfterMapping
    public default void additionalProperties(Actor element, @MappingTarget WfeUser target) {
        target.setType(WfeExecutor.Type.USER);
    }

    default Actor map(WfeUser dto) {
        Actor actor = new Actor(dto.getName(), null);
        fill(actor, dto);
        return actor;
    }

    default void fill(Actor actor, WfeUser dto) {
        actor.setName(dto.getName());
        actor.setDescription(dto.getDescription());
        actor.setFullName(dto.getFullName());
        actor.setCode(dto.getCode());
        actor.setEmail(dto.getEmail());
        actor.setPhone(dto.getPhone());
        actor.setTitle(dto.getTitle());
        actor.setDepartment(dto.getDepartment());
    }
}
