package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

/**
 * Check that parameter of defined type is selected from dropdown list.
 */
public class IsFixedListPropertyOperation implements ReportParameterTypeVisitor<Boolean, Object> {

    public static final IsFixedListPropertyOperation INSTANCE = new IsFixedListPropertyOperation();

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
        return false;
    }

    @Override
    public Boolean onCheckedBoolean(Object data) {
        return false;
    }

    @Override
    public Boolean onProcessNameOrNull(Object data) {
        return true;
    }

    @Override
    public Boolean onSwimlane(Object data) {
        return true;
    }

    @Override
    public Boolean onActorId(Object data) {
        return true;
    }

    @Override
    public Boolean onGroupId(Object data) {
        return true;
    }

    @Override
    public Boolean onExecutorId(Object data) {
        return true;
    }

    @Override
    public Boolean onActorName(Object data) {
        return true;
    }

    @Override
    public Boolean onGroupName(Object data) {
        return true;
    }

    @Override
    public Boolean onExecutorName(Object data) {
        return true;
    }
}
