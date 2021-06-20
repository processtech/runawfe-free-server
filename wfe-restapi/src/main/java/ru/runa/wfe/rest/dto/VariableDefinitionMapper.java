package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.var.VariableDefinition;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface VariableDefinitionMapper {

    @Mapping(source = "userType.name", target = "userType")
    VariableDefinitionDto map(VariableDefinition definition);

    List<VariableDefinitionDto> map(List<VariableDefinition> definitions);
}
