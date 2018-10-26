package ru.runa.wfe.lang;

import java.util.List;

import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ScriptNode extends Node implements BoundaryEventContainer {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;
    private final List<BoundaryEvent> boundaryEvents = Lists.newArrayList();

    @Override
    public List<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }

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
    protected void execute(ExecutionContext executionContext) throws Exception {
        executionContext.addLog(new CurrentActionLog(this));
        ActionHandler actionHandler = delegation.getInstance();
        try {
            actionHandler.execute(executionContext);
        } catch (Exception e) {
            log.error("Handling failed using " + executionContext + " in " + this, e);
            if (hasErrorEventHandler()) {
                Utils.sendBpmnErrorMessage(executionContext.getProcess().getId(), getNodeId(), e);
                return;
            } else {
                throw e;
            }
        }
        leave(executionContext);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getNodeId()).add("delegation", delegation).toString();
    }
}
