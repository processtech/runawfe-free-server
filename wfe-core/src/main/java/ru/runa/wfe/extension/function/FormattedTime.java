package ru.runa.wfe.extension.function;

import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class FormattedTime extends Function<Object> {

    @Override
    protected Object doExecute(Object... parameters) {
        Date time;
        if (parameters.length >= 1) {
            if (!Date.class.isInstance(parameters[0])) {
                return null;
            }
            time = (Date) parameters[0];
        } else {
            time = new Date();
        }
        try {
            String s = CalendarUtil.format(time, CalendarUtil.HOURS_MINUTES_FORMAT);
            return CalendarUtil.convertToDate(s, CalendarUtil.HOURS_MINUTES_FORMAT);
        } catch (Exception e) {
            log.warn("Unparseable time", e);
        }
        return null;
    }

}
