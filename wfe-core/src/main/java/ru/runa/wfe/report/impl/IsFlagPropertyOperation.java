package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

/**
 * Check that parameter of defined type is selected from dropdown list.
 */
public class IsFlagPropertyOperation implements ReportParameterTypeVisitor<Boolean, Object> {

    public static final IsFlagPropertyOperation INSTANCE = new IsFlagPropertyOperation();

    @Override
    public Boolean onString(Object data) {
        return false;
    }

    @Override
    public Boolean onNumber(Object data) {
        return false;
    }

    @Override
    public Boolean onDate(Object data) {
        return false;
    }

    @Override
    public Boolean onUncheckedBoolean(Object data) {
        return true;
    }

    @Override
    public Boolean onCheckedBoolean(Object data) {
        return true;
    }

    @Override
    public Boolean onProcessNameOrNull(Object data) {
        return false;
    }

    @Override
    public Boolean onSwimlane(Object data) {
        return false;
    }
}
