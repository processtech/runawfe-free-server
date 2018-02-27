package ru.runa.wfe.extension.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class FormattedDate extends Function<Object> {

    @Override
    protected Object doExecute(Object... parameters) {
        Date date;
        if (parameters.length >= 1) {
            if (!Date.class.isInstance(parameters[0])) {
                return null;
            }
            date = (Date) parameters[0];
        } else {
            date = new Date();
        }
        try {
            return new SimpleDateFormat("dd.MM.yy").parse(new SimpleDateFormat("dd.MM.yy").format(date));
        } catch (ParseException e) {
            log.warn("Unparseable date", e);
        }
        return null;
    }

}
