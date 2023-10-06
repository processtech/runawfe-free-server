package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.runa.wfe.rest.dto.WfeVariable;
import ru.runa.wfe.rest.dto.WfeVariableType;
import ru.runa.wfe.var.dto.WfVariable;

@Mapper
public interface WfeVariableMapper {

    @Mapping(source = "definition.name", target = "name")
    public abstract WfeVariable map(WfVariable variable);

    public abstract List<WfeVariable> map(List<WfVariable> variables);

    @AfterMapping
    public default void afterMapping(WfVariable element, @MappingTarget WfeVariable target) {
        new VariableValueWrapper().process(element.getDefinition(), target);
        target.setType(WfeVariableType.findByJavaClass(element.getDefinition().getFormatNotNull().getJavaClass()));
        if (target.getType() == null) {
            throw new RuntimeException("Format is not supported in API: " + element.getDefinition().getFormat());
        }
        target.setFormat(element.getDefinition().getFormatNotNull().getName());
    }

}
