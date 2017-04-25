package ru.runa.wfe.report.impl;

import com.google.common.base.Strings;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

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

    @Override
    public Boolean onActorId(String data) {
        return true;
    }

    @Override
    public Boolean onGroupId(String data) {
        return true;
    }

    @Override
    public Boolean onExecutorId(String data) {
        return true;
    }

    @Override
    public Boolean onActorName(String data) {
        return true;
    }

    @Override
    public Boolean onGroupName(String data) {
        return true;
    }

    @Override
    public Boolean onExecutorName(String data) {
        return true;
    }
}
