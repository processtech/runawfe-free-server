package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Info log from Groovy Action.
 * 
 * @author vromav
 */
@Entity
@DiscriminatorValue(value = "J")
public class ArchivedNodeInfoLog extends ArchivedNodeLog implements NodeInfoLog {
    private static final long serialVersionUID = 1L;

    public ArchivedNodeInfoLog() {
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeInfoLog(this);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttribute(ATTR_PARAM) };
    }
}
