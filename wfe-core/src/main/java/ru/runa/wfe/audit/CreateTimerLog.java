package ru.runa.wfe.audit;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * Logging timer creation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "C")
public class CreateTimerLog extends ProcessLog {
    private static final long serialVersionUID = 1L;

    public CreateTimerLog() {
    }

    public CreateTimerLog(Date dueDate) {
        addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(dueDate));
    }

    @Transient
    @Override
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerLog(this);
    }
}
