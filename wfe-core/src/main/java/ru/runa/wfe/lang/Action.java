package ru.runa.wfe.lang;

import ru.runa.wfe.audit.CurrentActionLog;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

public class Action extends GraphElement {
    private static final long serialVersionUID = 1L;
    private Delegation delegation;
    private ActionEvent actionEvent;

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(delegation, "delegation in " + this);
        Preconditions.checkNotNull(actionEvent, "event in " + this);
        Preconditions.checkNotNull(parentElement, "parent in " + this);
    }

    public void execute(ExecutionContext executionContext) {
        try {
            ActionHandler actionHandler = delegation.getInstance();
            log.debug("Executing " + this);
            actionHandler.execute(executionContext);
            executionContext.addLog(new CurrentActionLog(this));
        } catch (Exception e) {
            log.error("Failed " + this);
            throw Throwables.propagate(e);
        }
    }

    public ActionEvent getEvent() {
        return actionEvent;
    }

    public void setEvent(ActionEvent actionEvent) {
        this.actionEvent = actionEvent;
    }

    public Delegation getDelegation() {
        return delegation;
    }

    public void setDelegation(Delegation instantiatableDelegate) {
        delegation = instantiatableDelegate;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("event", actionEvent).add("delegation", delegation).toString();
    }
}
