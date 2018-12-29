package ru.runa.wfe.validation.impl;

import java.util.Calendar;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.var.format.DateFormat;

public class RelativeToCurrentDateRangeFieldValidator extends RangeFieldValidator<Date> {

    @Autowired
    private BusinessCalendar businessCalendar;

    protected boolean useBusinessCalendar() {
        return getParameter(boolean.class, "useBusinessCalendar", false);
    }

    protected boolean ignoreTime() {
        return getParameter(boolean.class, "ignoreTime", false)
                || DateFormat.class.getName().equals(getVariableProvider().getVariableNotNull(getFieldName()).getDefinition().getFormat());
    }

    private Date getParameter(String name, boolean add) {
        Integer daysCount = getParameter(Integer.class, name, null);
        if (daysCount == null) {
            return null;
        }
        if (!add && daysCount != 0) {
            daysCount = -1 * daysCount;
        }
        Calendar result;
        if (useBusinessCalendar()) {
            Date date = businessCalendar.apply(new Date(), daysCount + " business days");
            result = CalendarUtil.dateToCalendar(date);
        } else {
            result = Calendar.getInstance();
            result.add(Calendar.DAY_OF_MONTH, daysCount);
        }
        if (ignoreTime()) {
            if (add) {
                CalendarUtil.setLastSecondTimeCalendar(result);
            } else {
                CalendarUtil.setZeroTimeCalendar(result);
            }
        }
        return result.getTime();
    }

    @Override
    protected Date getMaxComparatorValue(Class<Date> clazz) {
        return getParameter("max", true);
    }

    @Override
    protected Date getMinComparatorValue(Class<Date> clazz) {
        return getParameter("min", false);
    }

}
