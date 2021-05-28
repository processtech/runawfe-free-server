package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.task.dto.WfTask;

@Mapper(uses = WfVariableMapper.class)
public abstract class WfTaskMapper {

    @Mapping(source = "creationDate", target = "createDate")
    public abstract WfTaskDto map(WfTask task);

    public abstract List<WfTaskDto> map(List<WfTask> task);
    
}
