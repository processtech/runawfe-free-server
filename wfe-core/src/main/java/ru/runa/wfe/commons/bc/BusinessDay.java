package ru.runa.wfe.commons.bc;

import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.commons.CalendarInterval;

import com.google.common.collect.Lists;

public class BusinessDay {
    private final List<CalendarInterval> workingIntervals;
    public static final BusinessDay HOLIDAY = new BusinessDay(new ArrayList<CalendarInterval>());

    public BusinessDay(List<CalendarInterval> workingIntervals) {
        this.workingIntervals = workingIntervals;
    }

    public List<CalendarInterval> getWorkingIntervals() {
        return Lists.newArrayList(workingIntervals);
    }

    public boolean isHoliday() {
        return workingIntervals.size() == 0;
    }

    @Override
    public String toString() {
        if (isHoliday()) {
            return "holiday";
        }
        StringBuffer b = new StringBuffer();
        for (CalendarInterval interval : workingIntervals) {
            b.append("[").append(interval.toTimeRangeString()).append("]");
        }
        return b.toString();
    }
}
