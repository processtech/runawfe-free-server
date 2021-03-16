package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.execution.dto.WfProcess;

@Mapper(uses = WfVariableMapper.class)
public interface WfProcessMapper {
    WfProcessDto map(WfProcess process);

    List<WfProcessDto> map(List<WfProcess> process);
}
