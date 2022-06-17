package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.runa.wfe.rest.dto.WfeVariable;
import ru.runa.wfe.rest.dto.WfeVariableDefinition;
import ru.runa.wfe.var.VariableDefinition;

@Mapper
public interface WfeVariableDefinitionMapper {

    WfeVariableDefinition map(VariableDefinition definition);

    List<WfeVariableDefinition> map(List<VariableDefinition> definitions);

    @AfterMapping
    public default void afterMapping(VariableDefinition element, @MappingTarget WfeVariable target) {
        new VariableValueWrapper().process(element, target);
    }

}
