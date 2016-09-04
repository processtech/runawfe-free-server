package ru.runa.wfe.report.impl;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.report.ParameterValidationResult;
import ru.runa.wfe.report.ReportParameterType.ReportParameterTypeVisitor;

public class ReportParameterValidateOperation implements ReportParameterTypeVisitor<ParameterValidationResult, ReportParameterModel> {

    @Override
    public ParameterValidationResult onString(ReportParameterModel parameterModel) {
        return ParameterValidationResult.correctValidationResult();
    }

    @Override
    public ParameterValidationResult onNumber(ReportParameterModel parameterModel) {
        try {
            Long.parseLong(parameterModel.getValue());
            return ParameterValidationResult.correctValidationResult();
        } catch (Exception e) {
            return ParameterValidationResult.errorValidationResult(parameterModel.getName(), "Value " + parameterModel.getValue()
                    + " is not all digits");
        }
    }

    @Override
    public ParameterValidationResult onDate(ReportParameterModel parameterModel) {
        try {
            CalendarUtil.convertToDate(parameterModel.getValue(), CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
            return ParameterValidationResult.correctValidationResult();
        } catch (Exception e) {
            return ParameterValidationResult.errorValidationResult(parameterModel.getName(), "Value " + parameterModel.getValue()
                    + " is not correct date");
        }
    }

    @Override
    public ParameterValidationResult onUncheckedBoolean(ReportParameterModel parameterModel) {
        String value = parameterModel.getValue();
        try {
            if (value == null) {
                return ParameterValidationResult.correctValidationResult();
            }
            Boolean.parseBoolean(value);
            return ParameterValidationResult.correctValidationResult();
        } catch (Exception e) {
            return ParameterValidationResult.errorValidationResult(parameterModel.getName(), "Value " + value + " is not boolean");
        }
    }

    @Override
    public ParameterValidationResult onCheckedBoolean(ReportParameterModel parameterModel) {
        return onUncheckedBoolean(parameterModel);
    }

    @Override
    public ParameterValidationResult onProcessNameOrNull(ReportParameterModel parameterModel) {
        return ParameterValidationResult.correctValidationResult();
    }

    @Override
    public ParameterValidationResult onSwimlane(ReportParameterModel data) {
        return ParameterValidationResult.correctValidationResult();
    }
}
