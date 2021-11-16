package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.definition.dto.WfDefinition;

@Mapper(uses = WfUserDtoMapper.class)
public interface WfDefinitionMapper {
    WfDefinitionDto map(WfDefinition definition);

    List<WfDefinitionDto> map(List<WfDefinition> definitions);
}
