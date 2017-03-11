package ru.runa.wfe.commons.bc;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import ru.runa.wfe.commons.CalendarInterval;
import ru.runa.wfe.commons.CalendarUtil;

public class DefaultBusinessCalendar extends AbstractBusinessCalendar {
    private static Map<Integer, BusinessDay> WEEK_DAYS;
    private static List<Calendar> HOLIDAYS;

    private static synchronized void init() {
        if (WEEK_DAYS != null) {
            return;
        }
        WEEK_DAYS = Maps.newHashMap();
        HOLIDAYS = Lists.newArrayList();
        for (int weekDay = Calendar.SUNDAY; weekDay <= Calendar.SATURDAY; weekDay++) {
            WEEK_DAYS.put(weekDay, parse(BusinessCalendarProperties.getWeekWorkingTime(weekDay)));
        }
        for (String propertyName : BusinessCalendarProperties.getResources().getAllPropertyNames()) {
            if (propertyName.startsWith("holiday")) {
                String string = BusinessCalendarProperties.getResources().getStringProperty(propertyName);
                Calendar calendar = CalendarUtil.convertToCalendar(string, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
                CalendarUtil.setZeroTimeCalendar(calendar);
                HOLIDAYS.add(calendar);
            }
        }
    }

    private static BusinessDay parse(String string) {
        List<CalendarInterval> workingIntervals = Lists.newArrayList();
        StringTokenizer tokenizer = new StringTokenizer(string, "&");
        while (tokenizer.hasMoreTokens()) {
            String dayPartText = tokenizer.nextToken().trim();
            int separatorIndex = dayPartText.indexOf('-');
            if (separatorIndex == -1) {
                throw new IllegalArgumentException("improper format of interval '" + dayPartText + "'");
            }
            String fromText = dayPartText.substring(0, separatorIndex).trim().toLowerCase();
            String toText = dayPartText.substring(separatorIndex + 1).trim().toLowerCase();
            Date from = CalendarUtil.convertToDate(fromText, CalendarUtil.HOURS_MINUTES_FORMAT);
            Date to = CalendarUtil.convertToDate(toText, CalendarUtil.HOURS_MINUTES_FORMAT);
            workingIntervals.add(new CalendarInterval(from, to));
        }
        return new BusinessDay(workingIntervals);
    }

    @Override
    protected BusinessDay getBusinessDay(Calendar calendar) {
        init();
        calendar = CalendarUtil.getZeroTimeCalendar(calendar);
        if (HOLIDAYS.contains(calendar)) {
            return BusinessDay.HOLIDAY;
        }
        return WEEK_DAYS.get(calendar.get(Calendar.DAY_OF_WEEK));
    }

}
