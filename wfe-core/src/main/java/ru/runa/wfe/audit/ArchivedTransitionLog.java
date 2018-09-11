package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.Transition;

@Entity
@DiscriminatorValue(value = "T")
public class ArchivedTransitionLog extends ArchivedProcessLog implements TransitionLog {

    @Override
    @Transient
    public Type getType() {
        return Type.TRANSITION;
    }

    @Override
    @Transient
    public String getFromNodeId() {
        return getAttributeNotNull(ATTR_NODE_ID_FROM);
    }

    @Override
    @Transient
    public String getToNodeId() {
        return getAttributeNotNull(ATTR_NODE_ID_TO);
    }

    @Override
    @Transient
    public String getTransitionId() {
        return getAttributeNotNull(ATTR_TRANSITION_ID);
    }

    @Override
    @Transient
    public Transition getTransitionOrNull(ProcessDefinition processDefinition) {
        // due to process definition version update it can be null
        Node node = processDefinition.getNode(getFromNodeId());
        if (node == null) {
            return null;
        }
        return node.getLeavingTransition(getTransitionId());
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_TRANSITION_ID) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTransitionLog(this);
    }
}
