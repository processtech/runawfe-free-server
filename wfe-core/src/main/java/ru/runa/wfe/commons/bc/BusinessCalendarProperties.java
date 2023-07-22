package ru.runa.wfe.commons.bc;

import java.util.Calendar;

import ru.runa.wfe.commons.PropertyResources;

public class BusinessCalendarProperties {
    private static final PropertyResources RESOURCES = new PropertyResources("business.calendar.properties");

    public static PropertyResources getResources() {
        return RESOURCES;
    }

    @Deprecated
    public static int getBusinessDayInHours() {
        return RESOURCES.getIntegerProperty("business.day.expressed.in.hours", 8);
    }

    @Deprecated
    public static int getBusinessWeekInHours() {
        return RESOURCES.getIntegerProperty("business.week.expressed.in.hours", 40);
    }

    public static int getBusinessWeekInDays() {
        return RESOURCES.getIntegerProperty("business.week.expressed.in.business.days", 5);
    }

    public static int getBusinessMonthInDays() {
        return RESOURCES.getIntegerProperty("business.month.expressed.in.business.days", 21);
    }

    public static int getBusinessYearInDays() {
        return RESOURCES.getIntegerProperty("business.year.expressed.in.business.days", 220);
    }

    public static String getWeekWorkingTime(int weekDay) {
        String dayName;
        switch (weekDay) {
        case Calendar.MONDAY:
            dayName = "monday";
            break;
        case Calendar.TUESDAY:
            dayName = "tuesday";
            break;
        case Calendar.WEDNESDAY:
            dayName = "wednesday";
            break;
        case Calendar.THURSDAY:
            dayName = "thursday";
            break;
        case Calendar.FRIDAY:
            dayName = "friday";
            break;
        case Calendar.SATURDAY:
            dayName = "saturday";
            break;
        case Calendar.SUNDAY:
            dayName = "sunday";
            break;
        default:
            throw new IllegalArgumentException("weekday = " + weekDay);
        }
        return RESOURCES.getStringProperty("weekday." + dayName, "");
    }
}
