package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

/**
 * Logging process suspension.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "V")
public class CurrentProcessSuspendLog extends CurrentProcessLog implements ProcessSuspendLog {
    private static final long serialVersionUID = 1L;

    public CurrentProcessSuspendLog() {
    }

    public CurrentProcessSuspendLog(Actor actor) {
        setSeverity(Severity.DEBUG);
        setExecutorName(actor.getName());
    }

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_SUSPEND;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getExecutorNameNotNull()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessSuspendLog(this);
    }
}
