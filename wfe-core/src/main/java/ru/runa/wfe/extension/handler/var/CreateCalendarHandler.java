package ru.runa.wfe.extension.handler.var;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.bc.BusinessCalendar;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.extension.handler.var.CreateCalendarConfig.CalendarOperation;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.collect.Maps;

public class CreateCalendarHandler extends CommonHandler {
    private CreateCalendarConfig config;
    @Autowired
    private BusinessCalendar businessCalendar;

    @Override
    public void setConfiguration(String configuration) {
        config = CreateCalendarConfig.parse(configuration);
    }

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        Calendar calendar = Calendar.getInstance();
        if (config.getBaseVariableName() != null) {
            Date baseDate = variableProvider.getValueNotNull(Date.class, config.getBaseVariableName());
            calendar.setTime(baseDate);
        }
        config.applySubstitutions(variableProvider);
        for (CalendarOperation operation : config.getOperations()) {
            log.debug("Executing " + operation + " on " + CalendarUtil.formatDateTime(calendar));
            Integer amount = Integer.parseInt(operation.getExpression());
            if (CalendarOperation.ADD.equals(operation.getType())) {
                BusinessDuration duration = new BusinessDuration(operation.getField(), amount, operation.isBusinessTime());
                Date date = businessCalendar.apply(calendar.getTime(), duration);
                calendar.setTime(date);
            }
            if (CalendarOperation.SET.equals(operation.getType())) {
                calendar.set(operation.getField(), amount);
            }
            log.debug("Result: " + CalendarUtil.formatDateTime(calendar));
        }
        Map<String, Object> result = Maps.newHashMap();
        result.put(config.getOutVariableName(), calendar.getTime());
        return result;
    }

}
