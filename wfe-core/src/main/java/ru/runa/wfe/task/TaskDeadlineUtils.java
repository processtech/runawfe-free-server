package ru.runa.wfe.task;

import java.util.Date;

import ru.runa.wfe.commons.SystemProperties;

public class TaskDeadlineUtils {

    public static Date getDeadlineWarningDate(Task task) {
        return getDeadlineWarningDate(task.getCreateDate(), task.getDeadlineDate());
    }

    public static Date getDeadlineWarningDate(Date createDate, Date deadlineDate) {
        if (createDate == null || deadlineDate == null) {
            return null;
        }
        int percents = SystemProperties.getTaskAlmostDeadlineInPercents();
        long duration = deadlineDate.getTime() - createDate.getTime();
        return new Date(createDate.getTime() + duration * percents / 100);
    }

}
