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
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;

/**
 * a calendar that knows about business hours. modified on 06.03.2009 by
 * gavrusev_sergei
 */
public class JbpmBusinessCalendar implements BusinessCalendar {
    private final JbpmDay[] weekDays;
    private final List<JbpmHoliday> holidays;
    protected final Log log = LogFactory.getLog(getClass());

    public JbpmBusinessCalendar() {
        log.info("Using business calendar implementation: " + getClass());
        weekDays = JbpmDay.parseWeekDays(this);
        holidays = JbpmHoliday.parseHolidays();
    }

    public Date add(Date date, JbpmDuration duration) {
        if (duration.getMilliseconds() >= 0) {
            return addForward(date, duration);
        } else {
            return addBack(date, duration);
        }
    }

    public Date findStartOfNextDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        date = calendar.getTime();
        while (isHoliday(date)) {
            calendar.setTime(date);
            calendar.add(Calendar.DATE, 1);
            date = calendar.getTime();
        }
        return date;
    }

    public Date findEndOfPrevDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        date = calendar.getTime();
        while (isHoliday(date)) {
            calendar.setTime(date);
            calendar.add(Calendar.DATE, -1);
            date = calendar.getTime();
        }
        return date;
    }

    public JbpmDay findDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return weekDays[calendar.get(Calendar.DAY_OF_WEEK)];
    }

    @Override
    public boolean isHoliday(Calendar calendar) {
        return isHoliday(calendar.getTime());
    }

    private boolean isHoliday(Date date) {
        for (JbpmHoliday holiday : holidays) {
            if (holiday.includes(date)) {
                return true;
            }
        }
        return false;
    }

    JbpmDayPart findDayPart(Date date) {
        if (isHoliday(date)) {
            return null;
        }
        JbpmDay day = findDay(date);
        for (JbpmDayPart dayPart : day.getDayParts()) {
            if (dayPart.includes(date)) {
                return dayPart;
            }
        }
        return null;
    }

    public static Calendar getCalendar() {
        return new GregorianCalendar();
    }

    @Override
    public Date apply(Date date, String duration) {
        return add(date, new JbpmDuration(duration));
    }

    private Date addForward(Date date, JbpmDuration duration) {
        if (!duration.isBusinessTime()) {
            return duration.addTo(date);
        }
        JbpmDayPart dayPart = findDayPart(date);
        if (dayPart != null) {
            return dayPart.add(date, duration);
        }
        dayPart = findDay(date).findNextDayPartStart(0, date);
        date = dayPart.getStartTime(date);
        return dayPart.add(date, duration);
    }

    private Date addBack(Date date, JbpmDuration duration) {
        Date end = null;
        if (duration.isBusinessTime()) {
            JbpmDayPart dayPart = findDayPart(date);
            if (dayPart == null) {
                JbpmDay day = findDay(date);
                dayPart = day.findPrevDayPartEnd(day.getDayParts().length - 1, date);
                date = dayPart.getEndTime(date);
            }
            end = dayPart.add(date, duration);
        } else {
            end = duration.addTo(date);
        }
        return end;
    }
    
    @Override
    public Date apply(Date date, BusinessDuration duration) {
        throw new UnsupportedOperationException();
    }
}
