package ru.runa.wfe.lang;

import com.google.common.base.Preconditions;
import ru.runa.wfe.InternalApplicationException;

public class EventEmbeddedSubprocessNode extends SubprocessNode {
    @Override
    public NodeType getNodeType() {
        return NodeType.EVENT_SUBPROCESS;
    }

    @Override
    public void validate() {
        ((GraphElement) this).validate();
        Preconditions.checkNotNull(getSubProcessName(), "subProcessName in " + this);
        if (!getArrivingTransitions().isEmpty() || !getLeavingTransitions().isEmpty()) {
            throw new InternalApplicationException("Subprocess state for event embedded subprocess shouldn't have any transitions");
        }
    }

    @Override
    public boolean isEmbedded() {
        return true;
    }

    @Override
    public void setEmbedded(boolean embedded) {
    }
}
