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
