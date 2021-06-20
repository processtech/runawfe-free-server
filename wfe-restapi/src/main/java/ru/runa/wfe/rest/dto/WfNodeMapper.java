package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.lang.dto.WfNode;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface WfNodeMapper {
    WfNodeDto map(WfNode node);

    List<WfNodeDto> map(List<WfNode> nodes);
}
