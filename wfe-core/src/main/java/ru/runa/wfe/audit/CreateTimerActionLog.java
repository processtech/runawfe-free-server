package ru.runa.wfe.audit;

import java.util.Date;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.lang.jpdl.CreateTimerAction;

/**
 * Logging timer creation.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "C")
public class CreateTimerActionLog extends ActionLog {
    private static final long serialVersionUID = 1L;

    public CreateTimerActionLog() {
    }

    public CreateTimerActionLog(CreateTimerAction action, Date dueDate) {
        super(action);
        addAttribute(ATTR_DUE_DATE, CalendarUtil.formatDateTime(dueDate));
    }

    @Transient
    @Override
    public Object[] getPatternArguments() {
        return new Object[] { getAttributeNotNull(ATTR_ACTION), getAttributeNotNull(ATTR_DUE_DATE) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onCreateTimerActionLog(this);
    }
}
