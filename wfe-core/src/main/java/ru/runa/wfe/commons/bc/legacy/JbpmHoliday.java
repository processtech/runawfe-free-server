package ru.runa.wfe.commons.bc.legacy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendarProperties;

/**
 * identifies a continuous set of days.
 */
public class JbpmHoliday implements Serializable {
    private static final long serialVersionUID = 1L;

    private Date fromDay = null;
    private Date toDay = null;

    public static List<JbpmHoliday> parseHolidays() {
        List<JbpmHoliday> holidays = new ArrayList<JbpmHoliday>();
        for (String key : BusinessCalendarProperties.getResources().getAllPropertyNames()) {
            if (key.startsWith("holiday")) {
                JbpmHoliday holiday = new JbpmHoliday(BusinessCalendarProperties.getResources().getStringProperty(key));
                holidays.add(holiday);
            }
        }
        return holidays;
    }

    public JbpmHoliday(String holidayText) {
        int separatorIndex = holidayText.indexOf('-');
        if (separatorIndex == -1) {
            fromDay = CalendarUtil.convertToDate(holidayText.trim(), CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
            toDay = fromDay;
        } else {
            String fromText = holidayText.substring(0, separatorIndex).trim();
            String toText = holidayText.substring(separatorIndex + 1).trim();
            fromDay = CalendarUtil.convertToDate(fromText, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
            toDay = CalendarUtil.convertToDate(toText, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
        }
        // now we are going to set the toDay to the end of the day, rather
        // then the beginning.
        // we take the start of the next day as the end of the toDay.
        Calendar calendar = JbpmBusinessCalendar.getCalendar();
        calendar.setTime(toDay);
        calendar.add(Calendar.DATE, 1);
        toDay = calendar.getTime();
    }

    public boolean includes(Date date) {
        return fromDay.getTime() <= date.getTime() && date.getTime() < toDay.getTime();
    }
}
