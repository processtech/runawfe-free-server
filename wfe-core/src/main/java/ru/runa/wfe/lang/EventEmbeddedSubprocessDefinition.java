package ru.runa.wfe.lang;

import com.google.common.collect.Lists;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.lang.bpmn2.EndToken;

import java.util.List;

public class EventEmbeddedSubprocessDefinition extends SubprocessDefinition {

    @Override
    public void validate() {
        ((GraphElement) this).validate();
        if (getStartNodes().isEmpty()) {
            throw new InternalApplicationException("Event embedded subprocess should have at least 1 start node");
        }
        int endNodesCount = 0;
        for (Node node : nodesList) {
            if (node instanceof EndNode) {
                throw new InternalApplicationException("In event embedded subprocess it is not allowed end state");
            }
            if (node instanceof EndToken || node instanceof ru.runa.wfe.lang.jpdl.EndToken) {
                throw new RuntimeException("There should be EmbeddedSubprocessEndNode");
            }
            if (node instanceof EmbeddedSubprocessEndNode) {
                endNodesCount++;
            }
        }
        if (endNodesCount == 0) {
            throw new RuntimeException("In event embedded subprocess there are should be at least 1 end node");
        }
    }

    public List<BoundaryEvent> getStartNodes() {
        List<BoundaryEvent> list = Lists.newArrayList();
        for (Node node : nodesList) {
            if (node instanceof BoundaryEvent && node.getArrivingTransitions().isEmpty()) {
                BoundaryEvent event = (BoundaryEvent) node;
                if (!event.getBoundaryEventInterrupting()) {
                    list.add(event);
                }
            }
        }
        return list;
    }
}
