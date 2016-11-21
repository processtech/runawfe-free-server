package ru.runa.wfe.report.impl;

import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

/**
 * Operation to get HTML class for input element for parameter.
 * 
 */
public class ReportParameterHtmlClassOperation implements ReportParameterTypeVisitor<String, Object> {

    @Override
    public String onString(Object data) {
        return "";
    }

    @Override
    public String onNumber(Object data) {
        return "inputLong";
    }

    @Override
    public String onDate(Object data) {
        return "inputDate";
    }

    @Override
    public String onUncheckedBoolean(Object data) {
        return "";
    }

    @Override
    public String onCheckedBoolean(Object data) {
        return "";
    }

    @Override
    public String onProcessNameOrNull(Object data) {
        return "";
    }

    @Override
    public String onSwimlane(Object data) {
        return "";
    }
}
