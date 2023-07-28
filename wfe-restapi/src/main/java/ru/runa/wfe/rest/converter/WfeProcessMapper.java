package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.rest.dto.WfeProcess;

@Mapper(uses = WfeVariableMapper.class)
public interface WfeProcessMapper {

    @Mapping(source = "name", target = "definitionName")
    @Mapping(source = "version", target = "definitionVersion")
    WfeProcess map(WfProcess process);

    List<WfeProcess> map(List<WfProcess> process);
}
