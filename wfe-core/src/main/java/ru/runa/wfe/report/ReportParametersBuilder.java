package ru.runa.wfe.report;

import java.util.HashMap;
import java.util.List;

/**
 * Интерфейс компонентов, используемых для генерации данных, требуемых для построения отчетов типа {@link ReportConfigurationType.PARAMETER_BUILDER}.
 */
public interface ReportParametersBuilder {

    /**
     * Возвращает параметры, которые необходимо запросить у пользователя для построения отчета.
     * 
     * @return Параметры, которые необходимо запросить у пользователя для построения отчета.
     */
    public List<ReportParameter> getInputParameters();

    /**
     * Выполняет валидацию параметров, полученных от пользователя на корректность.
     * 
     * @param inputParameters
     *            Параметры, полученные от пользователя.
     * @return Возвращает список с результатами валидации параметров. Параметры прошедшие валидацию корректно могут отсутствовать в списке.
     */
    public List<ParameterValidationResult> validateInputParameters(HashMap<String, Object> inputParameters);

    /**
     * Возвращает детализованное название отчета если применимо.
     * 
     * @param inputParameters
     *            Параметры, полученные от пользователя.
     * @param reportDescription
     *            DTO с описанием отчета.
     * @return Возвращает детализованное название отчета если применимо.
     */
    public String getReportName(HashMap<String, Object> inputParameters, ReportDefinition definition);

    /**
     * Генерирует данные, требующиеся для построения отчета.
     * 
     * @param inputParameters
     *            Параметры, полученные от пользователя.
     * @return Возвращает параметры, которые должны быть переданы в отчет. Параметры, полученные от пользователя будут добавлены в это множество
     *         позже.
     */
    public HashMap<String, Object> fillReportParameters(HashMap<String, Object> inputParameters);
}
