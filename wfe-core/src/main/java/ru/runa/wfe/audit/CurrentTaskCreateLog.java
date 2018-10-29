package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.task.Task;

/**
 * Logging task creation.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "1")
public class CurrentTaskCreateLog extends CurrentTaskLog implements TaskCreateLog {
    private static final long serialVersionUID = 1L;

    public CurrentTaskCreateLog() {
    }

    public CurrentTaskCreateLog(Task task) {
        super(task);
        if (task.getDeadlineDate() != null) {
            addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(task.getDeadlineDate()));
        }
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.TASK_CREATE;
    }

    @Override
    @Transient
    public String getDeadlineDateString() {
        return getAttribute(ATTR_DUE_DATE);
    }

    @Override
    @Transient
    public Date getDeadlineDate() {
        String dateAsString = getDeadlineDateString();
        if (dateAsString == null) {
            return null;
        }
        return CalendarUtil.convertToDate(dateAsString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getTaskName(), getDeadlineDateString() };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCreateLog(this);
    }
}
