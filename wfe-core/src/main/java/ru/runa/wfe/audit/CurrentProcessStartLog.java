package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

/**
 * Logging process creation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "I")
public class CurrentProcessStartLog extends CurrentProcessLog implements ProcessStartLog {
    private static final long serialVersionUID = 1L;

    public CurrentProcessStartLog() {
    }

    public CurrentProcessStartLog(Actor actor) {
        setSeverity(Severity.INFO);
        setExecutorName(actor != null ? actor.getName() : "system");
    }

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_START;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getExecutorNameNotNull()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessStartLog(this);
    }
}
