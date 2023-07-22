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

import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import ru.runa.wfe.commons.bc.BusinessCalendarProperties;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * interprets textual descriptions of a duration.
 * <p>
 * Syntax: &lt;quantity&gt; [business] &lt;unit&gt; <br />
 * Where
 * <ul>
 * <li>&lt;quantity&gt; is a piece of text that is parsable with
 * <code>NumberFormat.getNumberInstance().parse(quantity)</code>.</li>
 * <li>&lt;unit&gt; is one of {second, seconds, minute, minutes, hour, hours,
 * day, days, week, weeks, month, months, year, years}.</li>
 * <li>And adding the optional indication <code>business</code> means that only
 * business hours should be taken into account for this duration.</li>
 * </ul>
 * </p>
 */
public class JbpmDuration implements Serializable {
    private static final long serialVersionUID = 2L;

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;
    public static final long WEEK = 7 * DAY;
    public static final long MONTH = 30 * DAY;
    public static final long YEAR = 365 * DAY;

    public static final long BUSINESS_DAY;
    public static final long BUSINESS_WEEK;
    public static final long BUSINESS_MONTH;
    public static final long BUSINESS_YEAR;

    static {
        BUSINESS_DAY = HOUR * BusinessCalendarProperties.getBusinessDayInHours();
        BUSINESS_WEEK = HOUR * BusinessCalendarProperties.getBusinessWeekInHours();
        BUSINESS_MONTH = BUSINESS_DAY * BusinessCalendarProperties.getBusinessMonthInDays();
        BUSINESS_YEAR = BUSINESS_DAY * BusinessCalendarProperties.getBusinessYearInDays();
    }

    static Map<String, Integer> calendarFields = Maps.newHashMap();

    static {
        Integer millisecondField = Integer.valueOf(Calendar.MILLISECOND);
        calendarFields.put("millisecond", millisecondField);
        calendarFields.put("milliseconds", millisecondField);

        Integer secondField = Integer.valueOf(Calendar.SECOND);
        calendarFields.put("second", secondField);
        calendarFields.put("seconds", secondField);

        Integer minuteField = Integer.valueOf(Calendar.MINUTE);
        calendarFields.put("minute", minuteField);
        calendarFields.put("minutes", minuteField);

        Integer hourField = Integer.valueOf(Calendar.HOUR);
        calendarFields.put("hour", hourField);
        calendarFields.put("hours", hourField);

        Integer dayField = Integer.valueOf(Calendar.DAY_OF_MONTH);
        calendarFields.put("day", dayField);
        calendarFields.put("days", dayField);

        Integer weekField = Integer.valueOf(Calendar.WEEK_OF_MONTH);
        calendarFields.put("week", weekField);
        calendarFields.put("weeks", weekField);

        Integer monthField = Integer.valueOf(Calendar.MONTH);
        calendarFields.put("month", monthField);
        calendarFields.put("months", monthField);

        Integer yearField = Integer.valueOf(Calendar.YEAR);
        calendarFields.put("year", yearField);
        calendarFields.put("years", yearField);
    }

    static Map<String, Long> businessAmounts = Maps.newHashMap();

    static {
        Long secondAmount = Long.valueOf(SECOND);
        businessAmounts.put("business second", secondAmount);
        businessAmounts.put("business seconds", secondAmount);

        Long minuteAmount = Long.valueOf(MINUTE);
        businessAmounts.put("business minute", minuteAmount);
        businessAmounts.put("business minutes", minuteAmount);

        Long hourAmount = Long.valueOf(HOUR);
        businessAmounts.put("business hour", hourAmount);
        businessAmounts.put("business hours", hourAmount);

        Long dayAmount = Long.valueOf(BUSINESS_DAY);
        businessAmounts.put("business day", dayAmount);
        businessAmounts.put("business days", dayAmount);

        Long weekAmount = Long.valueOf(BUSINESS_WEEK);
        businessAmounts.put("business week", weekAmount);
        businessAmounts.put("business weeks", weekAmount);

        Long monthAmount = Long.valueOf(BUSINESS_MONTH);
        businessAmounts.put("business month", monthAmount);
        businessAmounts.put("business months", monthAmount);

        Long yearAmount = Long.valueOf(BUSINESS_YEAR);
        businessAmounts.put("business year", yearAmount);
        businessAmounts.put("business years", yearAmount);
    }

    private int field;
    private long amount;
    private boolean businessTime;

    JbpmDuration() {
    }

    public JbpmDuration(long milliseconds) {
        amount = milliseconds;
        field = Calendar.MILLISECOND;
    }

    /**
     * creates a duration from a textual description. syntax: {number} space
     * {unit} where number is parsable to a java.lang.Number and unit is one of
     * <ul>
     * <li>second</li>
     * <li>seconds</li>
     * <li>minute</li>
     * <li>minutes</li>
     * <li>hour</li>
     * <li>hours</li>
     * <li>day</li>
     * <li>days</li>
     * <li>week</li>
     * <li>weeks</li>
     * <li>month</li>
     * <li>months</li>
     * <li>year</li>
     * <li>years</li>
     * </ul>
     */
    public JbpmDuration(String duration) {
        Preconditions.checkNotNull(duration, "duration is null");
        int index = indexOfNonWhite(duration, 0);
        char lead = duration.charAt(index);
        if (lead == '+' || lead == '-') {
            ++index;
        }
        // parse quantity
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        index = indexOfNonWhite(duration, index);
        ParsePosition position = new ParsePosition(index);
        Number quantity = format.parse(duration, position);
        if (quantity == null) {
            throw new IllegalArgumentException("improper format of duration '" + duration + "'");
        }

        String unitText = duration.substring(position.getIndex()).trim();
        if (unitText.startsWith("business")) {
            // parse unit
            Long unit = businessAmounts.get(unitText);
            if (unit == null) {
                throw new IllegalArgumentException("improper format of duration '" + duration + "'");
            }

            field = Calendar.MILLISECOND;
            amount = quantity.longValue() * unit.longValue();
            businessTime = true;
        } else {
            // parse unit
            Integer unit = calendarFields.get(unitText);
            if (unit == null) {
                throw new IllegalArgumentException("improper format of duration '" + duration + "'");
            }

            // is quantity exactly representable as int?
            if (quantity instanceof Long && isInteger(quantity.longValue())) {
                field = unit.intValue();
                amount = quantity.longValue();
            } else {
                field = Calendar.MILLISECOND;

                switch (unit.intValue()) {
                case Calendar.SECOND:
                    amount = (long) (quantity.doubleValue() * SECOND);
                    break;
                case Calendar.MINUTE:
                    amount = (long) (quantity.doubleValue() * MINUTE);
                    break;
                case Calendar.HOUR:
                    amount = (long) (quantity.doubleValue() * HOUR);
                    break;
                case Calendar.DAY_OF_MONTH:
                    amount = (long) (quantity.doubleValue() * DAY);
                    break;
                case Calendar.WEEK_OF_MONTH:
                    amount = (long) (quantity.doubleValue() * WEEK);
                    break;
                case Calendar.MONTH:
                    amount = (long) (quantity.doubleValue() * MONTH);
                    break;
                case Calendar.YEAR:
                    amount = (long) (quantity.doubleValue() * YEAR);
                    break;
                default:
                    throw new IllegalArgumentException("fractional amount not supported for unit '" + unitText + "'");
                }
            }
        }

        if (lead == '-') {
            amount = -amount;
        }
    }

    private static int indexOfNonWhite(String str, int fromIndex) {
        int off = fromIndex;
        int len = str.length();
        while (off < len && str.charAt(off) <= ' ') {
            off++;
        }
        return off;
    }

    private static boolean isInteger(long number) {
        return number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE;
    }

    public Date addTo(Date date) {
        if (field == Calendar.MILLISECOND) {
            return new Date(date.getTime() + amount);
        }

        Calendar calendar = JbpmBusinessCalendar.getCalendar();
        calendar.setTime(date);
        calendar.add(field, (int) amount);
        return calendar.getTime();
    }

    public long getMilliseconds() {
        switch (field) {
        case Calendar.MILLISECOND:
            return amount;
        case Calendar.SECOND:
            return amount * SECOND;
        case Calendar.MINUTE:
            return amount * MINUTE;
        case Calendar.HOUR:
            return amount * HOUR;
        case Calendar.DAY_OF_MONTH:
            return amount * DAY;
        case Calendar.WEEK_OF_MONTH:
            return amount * WEEK;
        case Calendar.MONTH:
            return amount * MONTH;
        case Calendar.YEAR:
            return amount * YEAR;
        default:
            throw new UnsupportedOperationException("calendar field '" + field + "' does not have a fixed duration");
        }
    }

    public boolean isBusinessTime() {
        return businessTime;
    }

}
