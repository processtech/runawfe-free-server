package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.commons.CalendarUtil;

@Entity
@DiscriminatorValue(value = "C")
@SuppressWarnings("unused")
public class ArchivedCreateTimerLog extends ArchivedNodeLog implements CreateTimerLog {

    @Override
    @Transient
    public Type getType() {
        return Type.CREATE_TIMER;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    @Transient
    public Date getDueDate() {
        return CalendarUtil.convertToDate(getAttributeNotNull(ATTR_DUE_DATE), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerLog(this);
    }
}
