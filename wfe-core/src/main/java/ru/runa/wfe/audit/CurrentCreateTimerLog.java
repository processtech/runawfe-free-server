package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
}
