package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.rest.dto.WfeTask;
import ru.runa.wfe.task.dto.WfTask;

@Mapper(uses = WfeVariableMapper.class)
public abstract class WfeTaskMapper {

    @Mapping(source = "creationDate", target = "createDate")
    @Mapping(source = "definitionVersionId", target = "definitionId")
    @Mapping(source = "targetActor", target = "targetUser")
    public abstract WfeTask map(WfTask task);

    public abstract List<WfeTask> map(List<WfTask> task);
    
}
