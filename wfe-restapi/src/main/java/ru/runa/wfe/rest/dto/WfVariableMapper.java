package ru.runa.wfe.rest.dto;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.runa.wfe.var.dto.WfVariable;

@Mapper
public interface WfVariableMapper {

    @Mapping(source = "definition.name", target = "name")
    WfVariableDto map(WfVariable variable);

    List<WfVariableDto> map(List<WfVariable> variables);

}
