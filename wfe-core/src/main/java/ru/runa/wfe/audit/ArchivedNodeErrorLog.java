package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Logging node errors
 *
 * @author Alekseev Mikhail
 * @since #1923
 */
@Entity
@DiscriminatorValue(value = "H")
public class ArchivedNodeErrorLog extends ArchivedNodeLog implements NodeErrorLog {
    private static final long serialVersionUID = 3940080812294087447L;

    public ArchivedNodeErrorLog() {
    }

    @Transient
    public String getMessage() {
        return getAttributeNotNull(ATTR_MESSAGE);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeErrorLog(this);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[]{ getMessage() };
    }
}
