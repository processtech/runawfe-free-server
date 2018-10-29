package ru.runa.wfe.var.format;

import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;

public abstract class AbstractDateFormat extends VariableFormat {
    private final String format;

    public AbstractDateFormat(String format) {
        this.format = format;
    }

    @Override
    public Class<Date> getJavaClass() {
        return Date.class;
    }

    @Override
    protected String convertToStringValue(Object object) {
        return CalendarUtil.format((Date) object, format);
    }

    @Override
    protected Date convertFromStringValue(String source) {
        return CalendarUtil.convertToDate(source, format);
    }

    @Override
    public Object parseJSON(String json) {
        if (json == null) {
            return null;
        }
        return convertFromStringValue(json);
    }

    @Override
    protected Object convertToJSONValue(Object value) {
        return convertToStringValue(value);
    }
}
