package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.lang.Node;

/**
 * Logging timer creation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "C")
public class CurrentCreateTimerLog extends CurrentNodeLog implements CreateTimerLog {
    private static final long serialVersionUID = 1L;

    public CurrentCreateTimerLog() {
    }

    public CurrentCreateTimerLog(Node node, Date dueDate) {
        super(node);
        addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(dueDate));
    }

    @Override
    @Transient
    public Type getType() {
        return Type.CREATE_TIMER;
    }

    @Override
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
