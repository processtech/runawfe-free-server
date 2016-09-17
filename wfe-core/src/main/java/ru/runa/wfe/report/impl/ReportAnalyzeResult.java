package ru.runa.wfe.report.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;

public class ReportAnalyzeResult {

    private String message;

    /**
     * Additional parameters that must be defined in order to build report.
     */
    private List<ReportAdminParameterEditModel> requiredParameters;

    public List<ReportAdminParameterEditModel> getRequiredParameters() {
        return requiredParameters;
    }

    public void setRequiredParameters(List<ReportAdminParameterEditModel> requiredParameters) {
        this.requiredParameters = requiredParameters;
    }

    public void addRequiredParameter(ReportAdminParameterEditModel parameter) {
        if (requiredParameters == null) {
            requiredParameters = new ArrayList<ReportAdminParameterEditModel>();
        }
        requiredParameters.add(parameter);
        appendMessage("Parameter " + parameter.getInnerName() + " is not set but required for report construction");
    }

    public String getMessage() {
        return message;
    }

    public void appendMessage(String message) {
        if (Strings.isNullOrEmpty(this.message)) {
            this.message = message;
        } else {
            this.message = this.message + "; " + message;
        }
    }

    public void appendMessage(String parameterName, String message) {
        appendMessage("Parameter '" + parameterName + "': " + message);
    }
}
