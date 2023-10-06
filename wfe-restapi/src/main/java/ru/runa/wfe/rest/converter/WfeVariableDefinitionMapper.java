package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ru.runa.wfe.rest.dto.WfeVariableDefinition;
import ru.runa.wfe.rest.dto.WfeVariableType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;

@Mapper
public interface WfeVariableDefinitionMapper {

    WfeVariableDefinition map(VariableDefinition definition);

    List<WfeVariableDefinition> map(List<VariableDefinition> definitions);

    @AfterMapping
    public default void afterMapping(VariableDefinition element, @MappingTarget WfeVariableDefinition target) {
        target.setType(WfeVariableType.findByJavaClass(element.getFormatNotNull().getJavaClass()));
        if (target.getType() == null) {
            throw new RuntimeException("Format is not supported in API: " + element.getFormat());
        }
        target.setFormat(element.getFormatNotNull().getName());
        if (element.getFormatNotNull() instanceof ListFormat) {
            VariableFormat componentFormat = FormatCommons.createComponent(element, 0);
            target.setComponentType(WfeVariableType.findByJavaClass(componentFormat.getJavaClass()));
            if (target.getComponentType() == null) {
                throw new RuntimeException("Format is not supported in API: " + componentFormat);
            }
            if (componentFormat instanceof UserTypeFormat) {
                target.setComponentUserType(Mappers.getMapper(WfeVariableUserTypeMapper.class).map(element.getFormatComponentUserTypes()[0]));
            }
        }
    }

}
