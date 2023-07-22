package ru.runa.wfe.commons;

import java.util.Calendar;

public abstract class CalendarIterator {
    private final Calendar c;
    private final Calendar to;
    private final Calendar from;
    private final boolean asc;

    public CalendarIterator(Calendar from, Calendar to) {
        this.from = from;
        this.c = CalendarUtil.getZeroTimeCalendar(from);
        this.to = to;
        this.asc = true;
    }

    public CalendarIterator(CalendarInterval interval) {
        this.from = interval.getFrom();
        this.c = CalendarUtil.getZeroTimeCalendar(interval.getFrom());
        this.to = interval.getTo();
        this.asc = true;
    }

    public CalendarIterator(CalendarInterval interval, boolean asc) {
        this.from = interval.getFrom();
        this.to = interval.getTo();
        this.asc = asc;
        if (asc) {
            this.c = CalendarUtil.getZeroTimeCalendar(interval.getFrom());
        } else {
            this.c = CalendarUtil.getZeroTimeCalendar(interval.getTo());
        }
    }

    protected abstract void iteration(Calendar current);

    public final void iterate() {
        if (asc) {
            while (!c.after(to)) {
                iteration(c);
                c.add(Calendar.DAY_OF_YEAR, 1);
            }
        } else {
            while (!c.before(from)) {
                iteration(c);
                c.add(Calendar.DAY_OF_YEAR, -1);
            }
        }
    }

}
