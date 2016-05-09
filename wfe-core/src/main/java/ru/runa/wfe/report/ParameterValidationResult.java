package ru.runa.wfe.report;

/**
 * Результат валидации параметра, полученного от пользователя.
 */
public class ParameterValidationResult {
    /**
     * Название, под которым параметр должен быть передан в отчет.
     */
    private String innerReportParameterName;

    /**
     * Название, отображаемое пользователю.
     */
    private String reportParameterName;

    /**
     * Флаг, равный true, если параметр корректен и False иначе.
     */
    private boolean isCorrect;

    /**
     * Описание ошибки в случае некорректного параметра.
     */
    private String errorMessage;

    public ParameterValidationResult() {
        super();
    }

    /**
     * Создаёт результат с корректной валидацией параметра.
     * 
     * @return Возвращает результат с корректной валидацией параметра.
     */
    public static ParameterValidationResult correctValidationResult() {
        ParameterValidationResult result = new ParameterValidationResult();
        result.setCorrect(true);
        return result;
    }

    /**
     * Создаёт результат с некорректной валидацией параметра.
     * 
     * @return Возвращает результат с некорректной валидацией параметра.
     */
    public static ParameterValidationResult errorValidationResult(String reportParameterName, String message) {
        ParameterValidationResult result = new ParameterValidationResult();
        result.setCorrect(false);
        result.setReportParameterName(reportParameterName);
        result.setErrorMessage(message);
        return result;
    }

    /**
     * Название, под которым параметр должен быть передан в отчет.
     */
    public String getInnerReportParameterName() {
        return innerReportParameterName;
    }

    /**
     * Название, под которым параметр должен быть передан в отчет.
     */
    public void setInnerReportParameterName(String innerReportParameterName) {
        this.innerReportParameterName = innerReportParameterName;
    }

    /**
     * Флаг, равный true, если параметр корректен и False иначе.
     */
    public boolean isCorrect() {
        return isCorrect;
    }

    /**
     * Флаг, равный true, если параметр корректен и False иначе.
     */
    public void setCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
    }

    /**
     * Описание ошибки в случае некорректного параметра.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Описание ошибки в случае некорректного параметра.
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Название, отображаемое пользователю.
     */
    public String getReportParameterName() {
        return reportParameterName;
    }

    /**
     * Название, отображаемое пользователю.
     */
    public void setReportParameterName(String reportParameterName) {
        this.reportParameterName = reportParameterName;
    }
}
