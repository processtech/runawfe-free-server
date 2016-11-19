package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

import com.google.common.base.Strings;

/**
 * Operation of checking if the report parameter is set.
 */
public class ReportParameterIsSetOperation implements ReportParameterTypeVisitor<Boolean, String> {

    @Override
    public Boolean onString(String data) {
        return !Strings.isNullOrEmpty(data);
    }

    @Override
    public Boolean onNumber(String data) {
        return !Strings.isNullOrEmpty(data);
    }

    @Override
    public Boolean onDate(String data) {
        return !Strings.isNullOrEmpty(data);
    }

    @Override
    public Boolean onUncheckedBoolean(String data) {
        return true;
    }

    @Override
    public Boolean onCheckedBoolean(String data) {
        return true;
    }

    @Override
    public Boolean onProcessNameOrNull(String data) {
        return true;
    }

    @Override
    public Boolean onSwimlane(String data) {
        return true;
    }
}
