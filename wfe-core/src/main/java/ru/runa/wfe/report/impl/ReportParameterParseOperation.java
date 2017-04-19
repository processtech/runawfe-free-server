package ru.runa.wfe.report.impl;

import com.google.common.base.Strings;

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

    @Override
    public Object onActorId(String data) {
        return onExecutorId(data);
    }

    @Override
    public Object onGroupId(String data) {
        return onExecutorId(data);
    }

    @Override
    public Object onExecutorId(String data) {
        try {
            if (Strings.isNullOrEmpty(data)) {
                return null;
            }
            return Long.parseLong(data);
        } catch (Exception e) {
            throw new InternalApplicationException("Value " + data + " is not all digits", e);
        }
    }

    @Override
    public Object onActorName(String data) {
        return data;
    }

    @Override
    public Object onGroupName(String data) {
        return data;
    }

    @Override
    public Object onExecutorName(String data) {
        return data;
    }
}
