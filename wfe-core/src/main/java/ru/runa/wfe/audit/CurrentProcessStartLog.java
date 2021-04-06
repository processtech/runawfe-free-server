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
        addAttribute(ATTR_ACTOR_NAME, actor != null ? actor.getName() : "system");
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_START;
    }

    @Override
    @Transient
    public String getActorName() {
        return getAttributeNotNull(ATTR_ACTOR_NAME);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { new ExecutorNameValue(getActorName()) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessStartLog(this);
    }
}
