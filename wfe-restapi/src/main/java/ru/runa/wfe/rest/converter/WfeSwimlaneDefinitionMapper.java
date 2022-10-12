package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.rest.dto.WfeSwimlaneDefinition;

@Mapper
public interface WfeSwimlaneDefinitionMapper {
    WfeSwimlaneDefinition map(SwimlaneDefinition definition);

    List<WfeSwimlaneDefinition> map(List<SwimlaneDefinition> definitions);
}
