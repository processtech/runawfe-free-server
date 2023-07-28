package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;

public class TimeRangeValidator extends RangeFieldValidator<Date> {

    private Date getParameter(String name) {
        Calendar baseDate = TypeConversionUtil.convertTo(Calendar.class, getFieldValue());
        Calendar param = getParameter(Calendar.class, name, null);
        if (param == null) {
            return null;
        }
        CalendarUtil.setDateFromCalendar(param, baseDate);
        return param.getTime();
    }

    @Override
    protected Date getMaxComparatorValue(Class<Date> clazz) {
        return getParameter("max");
    }

    @Override
    protected Date getMinComparatorValue(Class<Date> clazz) {
        return getParameter("min");
    }

}
