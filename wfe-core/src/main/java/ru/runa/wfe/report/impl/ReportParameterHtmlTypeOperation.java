package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

/**
 * Operation to get HTML type for parameter.
 * 
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
}
