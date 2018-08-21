package ru.runa.wfe.audit;

import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

public interface TransitionLog extends ProcessLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TRANSITION;
    }

    @Transient
    default String getFromNodeId() {
        return getAttributeNotNull(ATTR_NODE_ID_FROM);
    }

    @Transient
    default String getToNodeId() {
        return getAttributeNotNull(ATTR_NODE_ID_TO);
    }

    @Transient
    default String getTransitionId() {
        return getAttributeNotNull(ATTR_TRANSITION_ID);
    }

    default Transition getTransitionOrNull(ProcessDefinition processDefinition) {
        // due to process definition version update it can be null
        Node node = processDefinition.getNode(getFromNodeId());
        if (node == null) {
            return null;
        }
        return node.getLeavingTransition(getTransitionId());
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_TRANSITION_ID) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTransitionLog(this);
    }
}
