package ru.runa.wfe.rest.converter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.runa.wfe.graph.view.MultiSubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.StartNodeGraphElement;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.TaskNodeGraphElement;
import ru.runa.wfe.rest.dto.WfeNodeGraphElement;

@Mapper(uses = WfeProcessLogMapper.class)
public interface WfeNodeGraphElementMapper {

    WfeNodeGraphElement map(NodeGraphElement element);

    List<WfeNodeGraphElement> map(List<NodeGraphElement> elements);

    @AfterMapping
    public default void additionalProperties(NodeGraphElement element, @MappingTarget WfeNodeGraphElement target) {
        Map<String, Object> result = new HashMap<>();
        if (element instanceof StartNodeGraphElement) {
            result.put("swimlaneName", ((StartNodeGraphElement) element).getSwimlaneName());
        }
        if (element instanceof TaskNodeGraphElement) {
            result.put("swimlaneName", ((TaskNodeGraphElement) element).getSwimlaneName());
            result.put("minimized", ((TaskNodeGraphElement) element).isMinimized());
        }
        if (element instanceof SubprocessNodeGraphElement) {
            SubprocessNodeGraphElement e = (SubprocessNodeGraphElement) element;
            result.put("subprocessId", e.getSubprocessId());
            result.put("subprocessAccessible", e.isSubprocessAccessible());
            result.put("subprocessName", e.getSubprocessName());
            result.put("embeddedSubprocess", e.isEmbedded());
            result.put("embeddedSubprocessId", e.getEmbeddedSubprocessId());
            result.put("embeddedSubprocessGraphWidth", e.getEmbeddedSubprocessGraphWidth());
            result.put("embeddedSubprocessGraphHeight", e.getEmbeddedSubprocessGraphHeight());
        }
        if (element instanceof MultiSubprocessNodeGraphElement) {
            MultiSubprocessNodeGraphElement e = (MultiSubprocessNodeGraphElement) element;
            result.put("subprocessIds", e.getSubprocessIds());
            result.put("accessibleSubprocessIds", e.getAccessibleSubprocessIds());
            result.put("completedSubprocessIds", e.getCompletedSubprocessIds());
        }
        target.setAdditionalProperties(result);
    }
}
