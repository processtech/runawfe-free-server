package ru.runa.wfe.report.impl;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

public class ReportParameterParseOperation implements ReportParameterTypeVisitor<Object, String> {

    @Override
    public Object onString(String data) {
        return data;
    }

    @Override
    public Object onNumber(String data) {
        try {
            return Long.parseLong(data);
        } catch (Exception e) {
            throw new InternalApplicationException("Value " + data + " is not all digits", e);
        }
    }

    @Override
    public Object onDate(String data) {
        try {
            return CalendarUtil.convertToDate(data, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
        } catch (Exception e) {
            throw new InternalApplicationException("Value " + data + " is not correct date", e);
        }
    }

    @Override
    public Object onUncheckedBoolean(String data) {
        try {
            if (data == null) {
                return false;
            }
            return Boolean.parseBoolean(data);
        } catch (Exception e) {
            throw new InternalApplicationException("Value " + data + " is not bolean", e);
        }
    }

    @Override
    public Object onCheckedBoolean(String data) {
        return onUncheckedBoolean(data);
    }

    @Override
    public Object onProcessNameOrNull(String data) {
        return data;
    }

    @Override
    public Object onSwimlane(String data) {
        return data;
    }
}
