package ru.runa.wfe.lang;

import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class ScriptNode extends Node {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;

    @Override
    public NodeType getNodeType() {
        return NodeType.ACTION_NODE;
    }

    public void setDelegation(Delegation delegation) {
        this.delegation = delegation;
    }

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation in " + this);
    }

    @Override
    public void execute(ExecutionContext executionContext) {
        log.debug("Executing " + this);
        try {
            executionContext.addLog(new ActionLog(this));
            ActionHandler actionHandler = delegation.getInstance();
            actionHandler.execute(executionContext);
        } catch (Exception e) {
            log.error("Failed " + this);
            throw Throwables.propagate(e);
        }
        leave(executionContext);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("id", getNodeId()).add("delegation", delegation).toString();
    }

}
