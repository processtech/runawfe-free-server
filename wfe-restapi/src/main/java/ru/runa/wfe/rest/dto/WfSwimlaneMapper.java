package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.execution.dto.WfSwimlane;
import java.util.List;

@Mapper(uses = SwimlaneDefinitionMapper.class)
public interface WfSwimlaneMapper {
    WfSwimlaneDto map(WfSwimlane swimlane);

    List<WfSwimlaneDto> map(List<WfSwimlane> swimlanes);
}
