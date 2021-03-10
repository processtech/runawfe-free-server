package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.task.dto.WfTask;

@Mapper(uses = WfVariableMapper.class)
public interface WfTaskMapper {

    WfTaskDto map(WfTask task);

    List<WfTaskDto> map(List<WfTask> task);

}
