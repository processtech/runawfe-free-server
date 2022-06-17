package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.runa.wfe.rest.dto.WfeExecutor;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

@Mapper
public abstract class WfeExecutorMapper {
    public abstract List<WfeExecutor> map(List<? extends Executor> executors);

    public WfeExecutor map(Executor executor) {
        if (executor == null) {
            return null;
        }
        if (executor instanceof Actor) {
            return Mappers.getMapper(WfeUserMapper.class).map((Actor) executor);
        }
        if (executor instanceof Group) {
            return Mappers.getMapper(WfeGroupMapper.class).map((Group) executor);
        }
        throw new IllegalArgumentException(executor.getClass().toString());
    }

}
