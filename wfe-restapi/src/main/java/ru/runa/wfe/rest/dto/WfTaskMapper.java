package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.logic.TaskLogic;

@Mapper(uses = WfVariableMapper.class,
        unmappedTargetPolicy = org.mapstruct.ReportingPolicy.IGNORE,
        componentModel = "spring")
public abstract class WfTaskMapper {

    @Autowired
    private TaskLogic taskLogic;
    
    @Mapping(expression = "java( getDefinitionCategory( task ) )", target = "category")
    public abstract WfTaskDto map(WfTask task);

    public abstract List<WfTaskDto> map(List<WfTask> task);
    
    public String getDefinitionCategory(WfTask task) {
        return taskLogic.getDefinition(task.getDefinitionVersionId()).getProcessDefinition().getCategory();
    }
}
