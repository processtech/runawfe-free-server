package ru.runa.wfe.report;

/**
 * User defined parameter validation result.
 */
public class ParameterValidationResult {
    /**
     * Parameter name for report.
     */
    private String innerReportParameterName;

    /**
     * Parameter name that is shown to user.
     */
    private String reportParameterName;

    /**
     * true if parameter is required and false if it's optional.
     */
    private boolean isCorrect;

    /**
     * Error description in case of invalid parameter.
     */
    private String errorMessage;

    public ParameterValidationResult() {
        super();
    }

    /**
     * Creates result with correct parameter validation.
     * 
     * @return result with correct parameter validation.
     */
    public static ParameterValidationResult correctValidationResult() {
        ParameterValidationResult result = new ParameterValidationResult();
        result.setCorrect(true);
        return result;
    }

    /**
     * Creates result with invalid parameter validation..
     * 
     * @return result with invalid parameter validation..
     */
    public static ParameterValidationResult errorValidationResult(String reportParameterName, String message) {
        ParameterValidationResult result = new ParameterValidationResult();
        result.setCorrect(false);
        result.setReportParameterName(reportParameterName);
        result.setErrorMessage(message);
        return result;
    }

    /**
     * Gets report parameter name.
     */
    public String getInnerReportParameterName() {
        return innerReportParameterName;
    }

    /**
     * Sets report parameter name.
     */
    public void setInnerReportParameterName(String innerReportParameterName) {
        this.innerReportParameterName = innerReportParameterName;
    }

    /**
     * True is parameter is correct, false if it's invalid
     */
    public boolean isCorrect() {
        return isCorrect;
    }

    /**
     * True is parameter is correct, false if it's invalid
     */
    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    /**
     *  Gets error description in case of invalid parameter.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     *  Sets error description in case of invalid parameter.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Gets name that is shown to user.
     */
    public String getReportParameterName() {
        return reportParameterName;
    }

    /**
     * Sets name that is shown to user.
     */
    public void setReportParameterName(String reportParameterName) {
        this.reportParameterName = reportParameterName;
    }
}
