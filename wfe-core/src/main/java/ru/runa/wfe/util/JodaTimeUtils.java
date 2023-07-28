package ru.runa.wfe.util;

import java.util.HashMap;
import java.util.Map;
import lombok.val;
import org.joda.time.Days;
import org.joda.time.DurationFieldType;
import org.joda.time.Hours;
import org.joda.time.Interval;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.Weeks;

public class JodaTimeUtils {

    /**
     * Adopted from: https://stackoverflow.com/a/22824236/4247442
     * Removed recursion, simplified.
     */
    public static int countFullPeriodsInInterval(Interval interval, Period period) {
        int guess = (int) (interval.toDurationMillis() / toAverageMillis(period));
        if (guess < 0) {
            return 0;
        }
        if (startPlusScaledPeriodIsAfterEnd(interval, period, guess + 1)) {
            while (startPlusScaledPeriodIsAfterEnd(interval, period, guess)) {
                guess--;
            }
        } else {
            while (!startPlusScaledPeriodIsAfterEnd(interval, period, guess + 1)) {
                guess++;
            }
        }
        return guess;
    }

    private static boolean startPlusScaledPeriodIsAfterEnd(Interval interval, Period period, int scalar) {
        return interval.getStart().plus(period.multipliedBy(scalar)).isAfter(interval.getEnd());
    }

    private static final long MILLIS_IN_DAY = Days.ONE.toStandardSeconds().getSeconds() * 1000L;
    private static final long MILLIS_IN_YEAR = Days.ONE.toStandardSeconds().getSeconds() * 365250L;

    private static final Map<DurationFieldType, Long> averageLengthMillis = new HashMap<DurationFieldType, Long>() {{
            put(DurationFieldType.millis(), 1L);
            put(DurationFieldType.seconds(), 1000L);
            put(DurationFieldType.minutes(), Minutes.ONE.toStandardSeconds().getSeconds() * 1000L);
            put(DurationFieldType.hours(), Hours.ONE.toStandardSeconds().getSeconds() * 1000L);
            put(DurationFieldType.halfdays(), MILLIS_IN_DAY / 2);
            put(DurationFieldType.days(), MILLIS_IN_DAY);
            put(DurationFieldType.weeks(), Weeks.ONE.toStandardSeconds().getSeconds() * 1000L);
            put(DurationFieldType.months(), MILLIS_IN_YEAR / 12);
            put(DurationFieldType.years(), MILLIS_IN_YEAR);
            put(DurationFieldType.weekyears(), MILLIS_IN_YEAR);
            put(DurationFieldType.centuries(), MILLIS_IN_YEAR * 100);
            put(DurationFieldType.eras(), Long.MAX_VALUE);
    }};

    private static long toAverageMillis(Period period) {
        long result = 0;
        for (val ft : period.getFieldTypes()) {
            result += period.get(ft) * averageLengthMillis.get(ft);
        }
        return result;
    }
}
