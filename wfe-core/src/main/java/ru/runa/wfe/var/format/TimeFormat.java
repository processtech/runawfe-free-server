package ru.runa.wfe.var.format;

import java.util.Date;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created on 30.11.2004
 *
 */
public class TimeFormat extends AbstractDateFormat {

    public TimeFormat() {
        super(CalendarUtil.HOURS_MINUTES_FORMAT);
    }

    @Override
    public String getName() {
        return "time";
    }

    @Override
    protected Date convertFromStringValue(String source) {
        Date date = super.convertFromStringValue(source);
        if (!CalendarUtil.areCalendarsEqualIgnoringTime(CalendarUtil.dateToCalendar(date), CalendarUtil.getZero())) {
            throw new InternalApplicationException("Time " + source + " does not belong to day range");
        }
        return date;
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onTime(this, context);
    }
}
