package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public abstract class ExecutorMapper {
    public abstract List<ExecutorDto> map(List<? extends Executor> executors);

    public ExecutorDto map(Executor executor) {
        if (executor instanceof Actor) {
            return Mappers.getMapper(WfUserDtoMapper.class).map((Actor) executor);
        }
        if (executor instanceof Group) {
            return Mappers.getMapper(WfGroupDtoMapper.class).map((Group) executor);
        }
        return new ExecutorDto(executor.getId(), executor.getName(), executor.getFullName());
    }
}
