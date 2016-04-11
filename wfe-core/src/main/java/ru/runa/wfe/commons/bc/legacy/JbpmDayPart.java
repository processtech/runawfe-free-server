/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.commons.bc.legacy;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * is part of a day that can for example be used to represent business hours.
 * 
 * modified on 06.03.2009 by gavrusev_sergei
 */
public class JbpmDayPart {
    int fromHour = -1;
    int fromMinute = -1;
    int toHour = -1;
    int toMinute = -1;
    JbpmDay day = null;
    int index = -1;

    public JbpmDayPart(String dayPartText, JbpmDay day, int index) {
        this.day = day;
        this.index = index;

        int separatorIndex = dayPartText.indexOf('-');
        if (separatorIndex == -1) {
            throw new IllegalArgumentException("improper format of daypart '" + dayPartText + "'");
        }
        String fromText = dayPartText.substring(0, separatorIndex).trim().toLowerCase();
        String toText = dayPartText.substring(separatorIndex + 1).trim().toLowerCase();

        Date from = CalendarUtil.convertToDate(fromText, CalendarUtil.HOURS_MINUTES_FORMAT);
        Date to = CalendarUtil.convertToDate(toText, CalendarUtil.HOURS_MINUTES_FORMAT);

        Calendar calendar = getCalendarWithDate(from);
        fromHour = calendar.get(Calendar.HOUR_OF_DAY);
        fromMinute = calendar.get(Calendar.MINUTE);

        calendar.setTime(to);
        toHour = calendar.get(Calendar.HOUR_OF_DAY);
        if (toHour == 0) {
            toHour = 24;
        }
        toMinute = calendar.get(Calendar.MINUTE);
    }

    public Date add(Date date, JbpmDuration duration) {
        if (duration.getMilliseconds() >= 0) {
            return moveForvard(date, duration);
        } else {
            return moveBack(date, duration);
        }
    }

    public boolean isStartAfter(Date date) {
        return date.compareTo(getStartTime(date)) <= 0;
    }

    public boolean isEndBefore(Date date) {
        return date.compareTo(getEndTime(date)) >= 0;
    }

    public boolean includes(Date date) {
        return date.compareTo(getStartTime(date)) >= 0 && date.compareTo(getEndTime(date)) <= 0;
    }

    public Date getStartTime(Date date) {
        Calendar calendar = getCalendarWithDate(date);
        calendar.set(Calendar.HOUR_OF_DAY, fromHour);
        calendar.set(Calendar.MINUTE, fromMinute);
        return calendar.getTime();
    }

    public Date getEndTime(Date date) {
        Calendar calendar = getCalendarWithDate(date);
        calendar.set(Calendar.HOUR_OF_DAY, toHour);
        calendar.set(Calendar.MINUTE, toMinute);
        return calendar.getTime();
    }

    private Date moveForvard(Date date, JbpmDuration duration) {
        Calendar calendar = getCalendarWithDate(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        long millisInThisDayPart = (toHour - hour) * JbpmDuration.HOUR + (toMinute - minute) * JbpmDuration.MINUTE;
        long durationMillis = duration.getMilliseconds();

        if (durationMillis <= millisInThisDayPart) {
            return duration.addTo(date);
        } else {
            JbpmDuration remainder = new JbpmDuration(durationMillis - millisInThisDayPart);
            Date dayPartEndDate = new Date(date.getTime() + millisInThisDayPart);
            JbpmDayPart nextDayPart = day.findNextDayPartStart(index + 1, dayPartEndDate);
            return nextDayPart.add(nextDayPart.getStartTime(dayPartEndDate), remainder);
        }
    }

    private Date moveBack(Date date, JbpmDuration duration) {
        Calendar calendar = getCalendarWithDate(date);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        long millisInThisDayPart = (fromHour - hour) * JbpmDuration.HOUR + (fromMinute - minute) * JbpmDuration.MINUTE;
        long durationMillis = duration.getMilliseconds();

        if (durationMillis >= millisInThisDayPart) {
            return duration.addTo(date);
        } else {
            JbpmDuration remainder = new JbpmDuration(durationMillis - millisInThisDayPart);
            Date dayPartStartDate = new Date(date.getTime() + millisInThisDayPart);
            JbpmDayPart nextDayPart = day.findPrevDayPartEnd(index - 1, dayPartStartDate);
            return nextDayPart.add(nextDayPart.getEndTime(dayPartStartDate), remainder);
        }
    }

    private Calendar getCalendarWithDate(Date date) {
        Calendar calendar = JbpmBusinessCalendar.getCalendar();
        calendar.setTime(date);
        return calendar;
    }
}
