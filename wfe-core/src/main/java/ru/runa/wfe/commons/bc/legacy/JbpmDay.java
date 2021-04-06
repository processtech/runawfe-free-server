package ru.runa.wfe.commons.bc.legacy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import ru.runa.wfe.commons.bc.BusinessCalendarProperties;

/**
 * is a day on a business calendar.
 */
public class JbpmDay {
    private JbpmDayPart[] dayParts;
    private JbpmBusinessCalendar businessCalendar;

    public static JbpmDay[] parseWeekDays(JbpmBusinessCalendar businessCalendar) {
        JbpmDay[] weekDays = new JbpmDay[8];
        for (int weekDay = Calendar.SUNDAY; weekDay <= Calendar.SATURDAY; weekDay++) {
            weekDays[weekDay] = new JbpmDay(BusinessCalendarProperties.getWeekWorkingTime(weekDay), businessCalendar);
        }
        return weekDays;
    }

    public JbpmDay(String dayPartsText, JbpmBusinessCalendar businessCalendar) {
        this.businessCalendar = businessCalendar;
        List<JbpmDayPart> dayPartsList = new ArrayList<JbpmDayPart>();
        StringTokenizer tokenizer = new StringTokenizer(dayPartsText, "&");
        while (tokenizer.hasMoreTokens()) {
            String dayPartText = tokenizer.nextToken().trim();
            dayPartsList.add(new JbpmDayPart(dayPartText, this, dayPartsList.size()));
        }
        dayParts = dayPartsList.toArray(new JbpmDayPart[dayPartsList.size()]);
    }

    public JbpmDayPart[] getDayParts() {
        return dayParts;
    }

    public JbpmDayPart findNextDayPartStart(int dayPartIndex, Date date) {
        // if there is a day part in this day that starts after the given date
        if (dayPartIndex < dayParts.length) {
            if (dayParts[dayPartIndex].isStartAfter(date)) {
                return dayParts[dayPartIndex];
            } else {
                return findNextDayPartStart(dayPartIndex + 1, date);
            }
        } else {
            // descend recursively
            date = businessCalendar.findStartOfNextDay(date);
            JbpmDay nextDay = businessCalendar.findDay(date);
            return nextDay.findNextDayPartStart(0, date);
        }
    }

    public JbpmDayPart findPrevDayPartEnd(int dayPartIndex, Date date) {
        // if there is a day part in this day that ends before the given date
        if (dayPartIndex >= 0) {
            if (dayParts[dayPartIndex].isEndBefore(date)) {
                return dayParts[dayPartIndex];
            } else {
                return findPrevDayPartEnd(dayPartIndex - 1, date);
            }
        } else {
            // descend recursively
            date = businessCalendar.findEndOfPrevDay(date);
            JbpmDay prevDay = businessCalendar.findDay(date);
            return prevDay.findPrevDayPartEnd(prevDay.dayParts.length - 1, date);
        }
    }
}
