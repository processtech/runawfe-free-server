package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.execution.dto.WfProcess;

@Mapper(uses = WfVariableMapper.class)
public interface WfProcessMapper {

    @Mapping(source = "name", target = "definitionName")
    WfProcessDto map(WfProcess process);

    List<WfProcessDto> map(List<WfProcess> process);
}
