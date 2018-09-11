package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Transient;

public interface TaskCreateLog extends TaskLog {

    @Transient
    String getDeadlineDateString();

    @Transient
    Date getDeadlineDate();
}
