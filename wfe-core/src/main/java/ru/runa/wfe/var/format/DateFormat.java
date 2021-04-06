package ru.runa.wfe.var.format;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * Created on 30.11.2004
 *
 */
public class DateFormat extends AbstractDateFormat {

    public DateFormat() {
        super(CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
    }

    @Override
    public String getName() {
        return "date";
    }

    @Override
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onDate(this, context);
    }
}
