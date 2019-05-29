package ru.runa.wfe.report;

import java.util.HashMap;
import java.util.List;

/**
 * Interfase for components that are used for data generation required for reports construction of type ReportConfigurationType.PARAMETER_BUILDER.
 */
public interface ReportParametersBuilder {

    /**
     * Return parameters that are required to be set by user in order to build report.
     * 
     * @return required parameters that are input by user to build report.
     */
    List<ReportParameter> getInputParameters();

    /**
     * Validates if parameters set by user are correct.
     * 
     * @param inputParameters
     *            Parameters set by user.
     * @return list with validation result. If parameter validates as a correct one it can be omitted from the list.
     */
    List<ParameterValidationResult> validateInputParameters(HashMap<String, Object> inputParameters);

    /**
     * Returns report name details if it can be applied.
     * 
     * @param inputParameters
     *            Parameters set by user.
     * @param definition
     *            DTO with report description.
     * @return report name details if it can be applied.
     */
    String getReportName(HashMap<String, Object> inputParameters, ReportDefinition definition);

    /**
     * Generates data required for report construction.
     * 
     * @param inputParameters
     *            Parameters set by user.
     * @return Parameters that must be supplied to the report. Parameters set by user are added to this map later.
     */
    HashMap<String, Object> fillReportParameters(HashMap<String, Object> inputParameters);
}
