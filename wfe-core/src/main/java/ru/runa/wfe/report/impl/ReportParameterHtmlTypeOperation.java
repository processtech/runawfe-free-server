package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

/**
 * Operation to get HTML type for parameter.
 */
public class ReportParameterHtmlTypeOperation implements ReportParameterTypeVisitor<String, Object> {

    @Override
    public String onString(Object data) {
        return "text";
    }

    @Override
    public String onNumber(Object data) {
        return "text";
    }

    @Override
    public String onDate(Object data) {
        return "text";
    }

    @Override
    public String onUncheckedBoolean(Object data) {
        return "checkbox";
    }

    @Override
    public String onCheckedBoolean(Object data) {
        return "checkbox";
    }

    @Override
    public String onProcessNameOrNull(Object data) {
        return "text";
    }

    @Override
    public String onSwimlane(Object data) {
        return "text";
    }

    @Override
    public String onActorId(Object data) {
        return "text";
    }

    @Override
    public String onGroupId(Object data) {
        return "text";
    }

    @Override
    public String onExecutorId(Object data) {
        return "text";
    }

    @Override
    public String onActorName(Object data) {
        return "text";
    }

    @Override
    public String onGroupName(Object data) {
        return "text";
    }

    @Override
    public String onExecutorName(Object data) {
        return "text";
    }
}
