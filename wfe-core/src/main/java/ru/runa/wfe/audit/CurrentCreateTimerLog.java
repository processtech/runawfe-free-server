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
public class CurrentCreateTimerLog extends CurrentProcessLog implements CreateTimerLog {
    private static final long serialVersionUID = 1L;

    public CurrentCreateTimerLog() {
    }

    public CurrentCreateTimerLog(Date dueDate) {
        addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(dueDate));
    }

    @Override
    @Transient
    public Type getType() {
        return Type.CREATE_TIMER;
    }

    @Transient
    public Date getDueDate() {
        return CalendarUtil.convertToDate(getAttributeNotNull(ATTR_DUE_DATE), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerLog(this);
    }
}
