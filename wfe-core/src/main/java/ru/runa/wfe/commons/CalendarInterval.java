package ru.runa.wfe.commons;

import java.util.Calendar;
import java.util.Date;

import com.google.common.base.Objects;

/**
 * Contains 2 calendars.
 * 
 * @author dofs
 */
public class CalendarInterval implements Comparable<CalendarInterval> {
    private Calendar from;
    private Calendar to;

    /**
     * Creates new calendar interval for whole day.
     */
    public CalendarInterval(Calendar onDate) {
        from = CalendarUtil.getZeroTimeCalendar(onDate);
        to = CalendarUtil.getLastSecondTimeCalendar(onDate);
    }

    /**
     * Creates new calendar interval by dates.
     */
    public CalendarInterval(Date from, Date to) {
        this(from, to, false);
    }

    /**
     * Creates new calendar interval by dates.
     * 
     * @param expandTimeInBounds
     *            if <code>true</code> then from time will be set to 00:00:00 and to time to 23:59:59
     */
    public CalendarInterval(Date from, Date to, boolean expandTimeInBounds) {
        this(CalendarUtil.dateToCalendar(from), CalendarUtil.dateToCalendar(to), expandTimeInBounds);
    }

    /**
     * Creates new calendar interval by calendars.
     */
    public CalendarInterval(Calendar from, Calendar to) {
        this(from, to, false);
    }

    /**
     * Creates new calendar interval by calendars.
     * 
     * @param expandTimeInBounds
     *            if <code>true</code> then from time will be set to 00:00:00 and to time to 23:59:59
     */
    public CalendarInterval(Calendar from, Calendar to, boolean expandTimeInBounds) {
        this.from = CalendarUtil.clone(from);
        this.to = CalendarUtil.clone(to);
        if (expandTimeInBounds) {
            CalendarUtil.setZeroTimeCalendar(this.from);
            CalendarUtil.setLastSecondTimeCalendar(this.to);
        }
    }

    public Calendar getFrom() {
        return from;
    }

    public Calendar getTo() {
        return to;
    }

    public void setFrom(Calendar from) {
        this.from = from;
    }

    public void setTo(Calendar to) {
        this.to = to;
    }

    /**
     * Check the right ordering of the dates
     */
    public boolean isValid() {
        return to.after(from);
    }

    public double getDaysBetween() {
        return CalendarUtil.daysBetween(from, to);
    }

    /**
     * by default it is inclusive operation
     */
    public boolean contains(Calendar calendar) {
        return !calendar.before(from) && !calendar.after(to);
    }

    public boolean contains(Calendar calendar, boolean inclusive) {
        if (inclusive) {
            return !calendar.before(from) && !calendar.after(to);
        } else {
            return calendar.after(from) && calendar.before(to);
        }
    }

    public boolean contains(CalendarInterval interval, boolean inclusive) {
        if (inclusive) {
            return !interval.getFrom().before(from) && !interval.getTo().after(to);
        } else {
            return interval.getFrom().after(from) && interval.getTo().before(to);
        }
    }

    public boolean intersects(CalendarInterval interval) {
        return interval.getFrom().before(to) && interval.getTo().after(from);
    }

    // returns true if there's an intersection or a gap between intervals is
    // smaller than the gapInMillis
    public boolean intersectsWithGapScale(CalendarInterval interval, int gapInMillis) {
        CalendarInterval gap = this.getGapBetweenNotIntersecting(interval);
        if (gap == null) {
            return true;
        }
        if (gap.getLengthInMillis() <= gapInMillis) {
            return true;
        }
        return false;
    }

    public CalendarInterval intersect(CalendarInterval interval) {
        if (from.before(interval.getFrom())) {
            from.setTimeInMillis(interval.getFrom().getTimeInMillis());
        }
        if (to.after(interval.getTo())) {
            to.setTimeInMillis(interval.getTo().getTimeInMillis());
        }
        return this;
    }

    public CalendarInterval getIntersection(CalendarInterval interval) {
        CalendarInterval target = CalendarUtil.clone(this);
        target.intersect(interval);
        return target;
    }

    public CalendarInterval getGapBetweenNotIntersecting(CalendarInterval interval) {
        if (this.intersects(interval)) {
            return null;
        }
        Calendar gapFrom = Calendar.getInstance();
        Calendar gapTo = Calendar.getInstance();
        if (from.before(interval.getFrom())) {
            gapFrom.setTime(to.getTime());
            gapTo.setTime(interval.getFrom().getTime());
        } else {
            gapFrom.setTime(interval.getTo().getTime());
            gapTo.setTime(from.getTime());
        }
        return new CalendarInterval(gapFrom, gapTo);
    }

    public long getLengthInMillis() {
        if (from != null && to != null) {
            return to.getTimeInMillis() - from.getTimeInMillis();
        }
        return 0;
    }

    public int getLengthInMinutes() {
        long millis = getLengthInMillis();
        return CalendarUtil.countMinutesFromMillis(millis);
    }

    public int getLengthInSeconds() {
        long millis = getLengthInMillis();
        return CalendarUtil.countSecondsFromMillis(millis);
    }

    @Override
    public String toString() {
        return CalendarUtil.format(from, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT) + "-"
                + CalendarUtil.format(to, CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
    }

    public String toDateRangeString() {
        return CalendarUtil.formatDate(from) + "-" + CalendarUtil.formatDate(to);
    }

    public String toTimeRangeString() {
        return CalendarUtil.formatTime(from) + "-" + CalendarUtil.formatTime(to);
    }

    public boolean hasEqualDates(CalendarInterval o) {
        if (from != null && o.getFrom() != null) {
            if (!CalendarUtil.areCalendarsEqualIgnoringTime(from, o.from)) {
                return false;
            }
        } else if (from == null && o.getFrom() != null) {
            return false;
        } else if (o.getFrom() == null && from != null) {
            return false;
        }

        if (to != null && o.getTo() != null) {
            if (!CalendarUtil.areCalendarsEqualIgnoringTime(to, o.to)) {
                return false;
            }
        } else if (to == null && o.getTo() != null) {
            return false;
        } else if (o.getTo() == null && to != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(from, to);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CalendarInterval)) {
            return false;
        }
        CalendarInterval o = (CalendarInterval) obj;
        return Objects.equal(from, o.from) && Objects.equal(to, o.to);
    }

    @Override
    public int compareTo(CalendarInterval interval) {
        int res = from.compareTo(interval.getFrom());
        if (res == 0) {
            res = to.compareTo(interval.getTo());
        }
        return res;
    }

}
