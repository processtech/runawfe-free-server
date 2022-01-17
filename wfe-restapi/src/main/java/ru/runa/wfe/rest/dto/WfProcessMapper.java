package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.execution.dto.WfProcess;

@Mapper(uses = WfVariableMapper.class)
public abstract class WfProcessMapper {

    @Mapping(source = "name", target = "definitionName")
    public abstract WfProcessDto map(WfProcess process);

    public abstract List<WfProcessDto> map(List<WfProcess> process);
}
