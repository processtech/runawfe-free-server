package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.definition.dto.WfDefinition;

@Mapper
public interface WfDefinitionMapper {
    WfDefinitionDto map(WfDefinition task);

    List<WfDefinitionDto> map(List<WfDefinition> task);
}
