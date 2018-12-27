package ru.runa.wfe.commons.bc;

import java.util.Calendar;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Represents business duration.
 */
public class BusinessDuration {
    private final int calendarField;
    private final int amount;
    private final boolean businessTime;

    public BusinessDuration(int calendarField, int amount, boolean businessTime) {
        if (businessTime) {
            if (Calendar.YEAR == calendarField) {
                amount *= BusinessCalendarProperties.getBusinessYearInDays();
                calendarField = Calendar.DAY_OF_YEAR;
            }
            if (Calendar.MONTH == calendarField) {
                amount *= BusinessCalendarProperties.getBusinessMonthInDays();
                calendarField = Calendar.DAY_OF_YEAR;
            }
            if (Calendar.WEEK_OF_YEAR == calendarField) {
                amount *= BusinessCalendarProperties.getBusinessWeekInDays();
                calendarField = Calendar.DAY_OF_YEAR;
            }
            if (Calendar.HOUR == calendarField) {
                amount *= 60;
                calendarField = Calendar.MINUTE;
            }
        }
        this.calendarField = calendarField;
        this.amount = amount;
        this.businessTime = businessTime;
    }

    public int getCalendarField() {
        return calendarField;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isBusinessTime() {
        return businessTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BusinessDuration)) {
            return false;
        }
        BusinessDuration d = (BusinessDuration) obj;
        return calendarField == d.calendarField && amount == d.amount && businessTime == d.businessTime;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(calendarField, amount, businessTime);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("amount", amount).add("field", calendarField).add("businessTime", businessTime).toString();
    }
}
