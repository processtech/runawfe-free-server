package ru.runa.wfe.task;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;
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

    /**
     * Returns String value containing period of time between start and end input dates. Returns negative value in case start date goes after end date
     *
     * @param startDate
     *            start of period
     * @param endDate
     *            end of period
     * @return string period in format "dd days hh:mm:ss"
     */
    public static String calculateTimeDuration(Date startDate, Date endDate) {
        String period = "";

        if (startDate == null || endDate == null) {
            return period;
        }

        Calendar endDateCal = CalendarUtil.dateToCalendar(endDate);
        Calendar startDateCal = CalendarUtil.dateToCalendar(startDate);

        int days = 0;
        long periodMillis = endDateCal.getTimeInMillis() - startDateCal.getTimeInMillis();

        boolean isStartDateBeforeEndDate = false;
        if (periodMillis < 0) {
            periodMillis *= -1;
            isStartDateBeforeEndDate = true;
            days = (int) CalendarUtil.daysBetween(endDateCal, startDateCal);
        } else {
            days = (int) CalendarUtil.daysBetween(startDateCal, endDateCal);
        }

        if (days > 1) {
            period = days + " days ";
        }

        Calendar periodCal = Calendar.getInstance();
        periodCal.setTimeInMillis(periodMillis - periodCal.getTimeZone().getOffset(periodMillis));
        period += CalendarUtil.format(periodCal, CalendarUtil.HOURS_MINUTES_SECONDS_FORMAT);

        if (isStartDateBeforeEndDate) {
            period = "- " + period;
        }

        return period;
    }
}
