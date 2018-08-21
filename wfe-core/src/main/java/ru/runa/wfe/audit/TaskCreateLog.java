package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Transient;
import ru.runa.wfe.commons.CalendarUtil;

public interface TaskCreateLog extends TaskLog {

    @Override
    @Transient
    default Type getType() {
        return Type.TASK_CREATE;
    }

    @Transient
    default String getDeadlineDateString() {
        return getAttribute(ATTR_DUE_DATE);
    }

    @Transient
    default Date getDeadlineDate() {
        String dateAsString = getDeadlineDateString();
        if (dateAsString == null) {
            return null;
        }
        return CalendarUtil.convertToDate(dateAsString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    @Override
    @Transient
    default Object[] getPatternArguments() {
        return new Object[] { getTaskName(), getDeadlineDateString() };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onTaskCreateLog(this);
    }
}
