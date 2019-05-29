package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

/**
 * Logging process activation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "U")
public class CurrentProcessActivateLog extends CurrentProcessLog implements ProcessActivateLog {
    private static final long serialVersionUID = 1L;

    public CurrentProcessActivateLog() {
    }

    public CurrentProcessActivateLog(Actor actor) {
        addAttribute(ATTR_ACTOR_NAME, actor != null ? actor.getName() : "system");
        setSeverity(Severity.DEBUG);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_ACTIVATE;
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
        visitor.onProcessActivateLog(this);
    }
}
