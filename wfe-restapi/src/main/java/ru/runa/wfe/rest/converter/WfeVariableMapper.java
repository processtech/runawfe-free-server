package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.runa.wfe.rest.dto.WfeVariable;
import ru.runa.wfe.var.dto.WfVariable;

@Mapper
public interface WfeVariableMapper {

    @Mapping(source = "definition.name", target = "name")
    @Mapping(source = "definition.format", target = "format")
    public abstract WfeVariable map(WfVariable variable);

    public abstract List<WfeVariable> map(List<WfVariable> variables);

    @AfterMapping
    public default void afterMapping(WfVariable element, @MappingTarget WfeVariable target) {
        new VariableValueWrapper().process(element.getDefinition(), target);
    }

}
