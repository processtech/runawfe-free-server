package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.rest.dto.WfeSwimlane;

@Mapper(uses = { WfeSwimlaneDefinitionMapper.class, WfeExecutorMapper.class })
public interface WfeSwimlaneMapper {
    WfeSwimlane map(WfSwimlane swimlane);

    List<WfeSwimlane> map(List<WfSwimlane> swimlanes);
}
