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
public class CreateTimerLog extends NodeLog {
    private static final long serialVersionUID = 1L;

    public CreateTimerLog() {
    }

    public CreateTimerLog(Node node, Date dueDate) {
        super(node);
        addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(dueDate));
    }

    @Transient
    public Date getDueDate() {
        return CalendarUtil.convertToDate(getAttributeNotNull(ATTR_DUE_DATE), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
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
