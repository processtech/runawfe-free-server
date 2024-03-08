package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.Transition;

/**
 * Logging transition passing.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "T")
public class CurrentTransitionLog extends CurrentProcessLog implements TransitionLog {
    private static final long serialVersionUID = 1L;

    public CurrentTransitionLog() {
    }

    public CurrentTransitionLog(Transition transition) {
        setNodeId(transition.getNodeId());
        setNodeName(transition.getName());
        addAttribute(ATTR_TRANSITION_ID, transition.getName());
        addAttribute(ATTR_NODE_ID_FROM, transition.getFrom().getTransitionNodeId(false));
        addAttribute(ATTR_NODE_ID_TO, transition.getTo().getTransitionNodeId(true));
    }

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
    public Transition getTransitionOrNull(ParsedProcessDefinition processDefinition) {
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
