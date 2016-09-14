package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;

import com.google.common.base.Objects;

public abstract class InterruptingNode extends Node {
    private static final long serialVersionUID = 1L;

    private boolean interrupting = true;

    public boolean isInterrupting() {
        return interrupting;
    }

    public void setInterrupting(boolean interrupting) {
        this.interrupting = interrupting;
    }

    @Override
    protected void execute(ExecutionContext executionContext) {
        if (!isInterrupting()) {
            Token oldToken = executionContext.getToken();
            Transition defaultLeavingTransition = getDefaultLeavingTransitionNotNull();
            Token newToken = new Token(oldToken, getNodeId() + "/" + defaultLeavingTransition.getNodeId());
            ExecutionContext newExecutionContext = new ExecutionContext(executionContext.getProcessDefinition(), newToken);
            super.leave(newExecutionContext, defaultLeavingTransition);
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getNodeId()).add("name", getName()).add("interrupting", isInterrupting()).toString();
    }
}
