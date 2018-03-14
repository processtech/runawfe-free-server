package ru.runa.wfe.extension.function;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class HoursRoundUp extends Function<Object> {

    public HoursRoundUp() {
        super(Param.required(Object.class));
    }

    @Override
    protected Object doExecute(Object... parameters) {
        Double doubleValue = (Double) translate(parameters[0], Double.class);
        if (doubleValue == null) {
            return null;
        }
        double minutes = doubleValue.doubleValue();
        long hours = (long) (minutes / 60);
        if (hours * 60 < minutes) {
            hours++;
        }
        return new Long(hours * 60);
    }

    private Object translate(Object o, Class<?> c) {
        if (c == String.class && Date.class.isInstance(o)) {
            Date date = (Date) o;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) == 1970 && calendar.get(Calendar.MONTH) == Calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                return CalendarUtil.format(date, CalendarUtil.HOURS_MINUTES_FORMAT);
            }
            if (calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
                return CalendarUtil.format(date, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
            }
            return CalendarUtil.format(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        }
        if (Date.class.isAssignableFrom(c) && Date.class.isInstance(o)) {
            return o;
        }
        return TypeConversionUtil.convertTo(c, o);
    }

    @Override
    public String getName() {
        return "hours_round_up";
    }

}
