package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Logging process finish.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "X")
public class CurrentProcessEndLog extends CurrentProcessLog implements ProcessEndLog {
    private static final long serialVersionUID = 1L;

    public CurrentProcessEndLog() {
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.PROCESS_END;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] {};
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onProcessEndLog(this);
    }
}
