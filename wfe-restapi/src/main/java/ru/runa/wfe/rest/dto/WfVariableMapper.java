package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.var.dto.WfVariable;

@Mapper(componentModel = "spring")
public abstract class WfVariableMapper {

    @Mapping(source = "definition.name", target = "name")
    @Mapping(source = "definition.format", target = "format")
    public abstract WfVariableDto map(WfVariable variable);

    public abstract List<WfVariableDto> map(List<WfVariable> variables);

}
