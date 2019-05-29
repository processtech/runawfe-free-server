package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created on 30.11.2004
 *
 */
public class DateTimeFormat extends AbstractDateFormat {

    public DateTimeFormat() {
        super(CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
    }

    @Override
    public String getName() {
        return "datetime";
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onDateTime(this, context);
    }
}
