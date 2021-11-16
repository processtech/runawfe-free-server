package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.lang.SwimlaneDefinition;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface SwimlaneDefinitionMapper {
    SwimlaneDefinitionDto map(SwimlaneDefinition definition);

    List<SwimlaneDefinitionDto> map(List<SwimlaneDefinition> definitions);
}
