package ru.runa.wfe.rest.dto;

import org.mapstruct.Mapper;
import ru.runa.wfe.graph.view.NodeGraphElement;
import java.util.List;

@Mapper(uses = WfVariableMapper.class)
public interface NodeGraphElementMapper {
    NodeGraphElementDto map(NodeGraphElement element);

    List<NodeGraphElementDto> map(List<NodeGraphElement> elements);
}
