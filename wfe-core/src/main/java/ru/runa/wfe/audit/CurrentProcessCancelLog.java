package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

/**
 * Logging process cancellation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "Y")
public class CurrentProcessCancelLog extends CurrentProcessLog implements ProcessCancelLog {
    private static final long serialVersionUID = 1L;

    public CurrentProcessCancelLog() {
    }

    public CurrentProcessCancelLog(Actor actor, String reason) {
        addAttribute(ATTR_ACTOR_NAME, actor.getName());
        addAttribute(ATTR_MESSAGE, reason);
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_CANCEL;
    }

    @Override
    @Transient
    public String getActorName() {
        return getAttribute(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        String reason = getAttribute(ATTR_MESSAGE);
        reason = reason != null ? " (" + reason + ")" : "";
        return new Object[] { new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)), reason };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessCancelLog(this);
    }
}
