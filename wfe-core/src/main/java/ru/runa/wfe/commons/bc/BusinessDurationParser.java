package ru.runa.wfe.commons.bc;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class BusinessDurationParser {
    private static final Map<String, Integer> calendarFields = Maps.newHashMap();
    static {
        calendarFields.put(DurationEnum.seconds.name(), Calendar.SECOND);
        calendarFields.put(DurationEnum.minutes.name(), Calendar.MINUTE);
        calendarFields.put(DurationEnum.hours.name(), Calendar.HOUR);
        calendarFields.put(DurationEnum.days.name(), Calendar.DAY_OF_YEAR);
        calendarFields.put(DurationEnum.weeks.name(), Calendar.WEEK_OF_YEAR);
        calendarFields.put(DurationEnum.months.name(), Calendar.MONTH);
        calendarFields.put(DurationEnum.years.name(), Calendar.YEAR);
    }

    /**
     * Creates a duration from a textual description. Syntax: &lt;quantity&gt; [business] &lt;unit&gt; <br />
     * unit is one of
     * <ul>
     * <li>seconds</li>
     * <li>minutes</li>
     * <li>hours</li>
     * <li>days</li>
     * <li>weeks</li>
     * <li>months</li>
     * <li>years</li>
     * </ul>
     */
    public static BusinessDuration parse(String durationString) {
        Preconditions.checkNotNull(durationString, "duration is null");
        durationString = durationString.trim();
        int index = indexOfNonWhite(durationString, 0);
        char lead = durationString.charAt(index);
        if (lead == '+' || lead == '-') {
            index++;
        }
        // parse quantity
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        index = indexOfNonWhite(durationString, index);
        ParsePosition position = new ParsePosition(index);
        Number quantity = format.parse(durationString, position);
        if (quantity == null) {
            throw new IllegalArgumentException("improper format of duration '" + durationString + "'");
        }
        String unitText = durationString.substring(position.getIndex()).trim();
        boolean businessTime = false;
        if (unitText.startsWith("business ")) {
            businessTime = true;
            unitText = unitText.substring("business ".length());
        }
        Integer unit = calendarFields.get(unitText);
        if (unit == null) {
            throw new IllegalArgumentException("improper format of duration '" + durationString + "'");
        }
        int calendarField = unit.intValue();
        int amount = quantity.intValue();
        if (lead == '-') {
            amount = -amount;
        }
        return new BusinessDuration(calendarField, amount, businessTime);
    }

    private static int indexOfNonWhite(String str, int off) {
        while (off < str.length() && Character.isWhitespace(str.charAt(off))) {
            off++;
        }
        return off;
    }

}
