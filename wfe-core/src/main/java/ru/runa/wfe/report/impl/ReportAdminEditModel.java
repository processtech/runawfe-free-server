package ru.runa.wfe.report.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.report.ReportConfigurationType;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportParameter;

/**
 * A model to edit parameters during deployment.
 */
public class ReportAdminEditModel {

    private Long id;

    /**
     * Report name that is shown to user.
     */
    private String name;

    /**
     * A set of parameters that user must define for report build.
     */
    private List<ReportAdminParameterEditModel> parameters;

    /**
     * Compiled jasper reports.
     */
    private byte[] compiledReport;

    /**
     * Report configuration type and report build type.
     */
    private ReportConfigurationType configType;

    /**
     * Removes empty parameters entries.
     */
    public void removeEntriesWithoutType() {
        Iterator<ReportAdminParameterEditModel> paramIter = parameters.iterator();
        while (paramIter.hasNext()) {
            ReportAdminParameterEditModel param = paramIter.next();
            if (param.getType() == null) {
                paramIter.remove();
            }
        }
    }

    public ReportAdminEditModel() {
        parameters = new ArrayList<ReportAdminParameterEditModel>();
    }

    public ReportAdminEditModel(ReportDefinition databaseDto, Map<String, String> paramNameToDescription) {
        id = databaseDto.getId();
        name = databaseDto.getName();
        parameters = new ArrayList<ReportAdminParameterEditModel>();
        if (databaseDto.getParameters() != null) {
            for (ReportParameter databaseParameterDto : databaseDto.getParameters()) {
                String description = paramNameToDescription.get(databaseParameterDto.getInnerName());
                parameters.add(new ReportAdminParameterEditModel(databaseParameterDto, description));
            }
        }
        compiledReport = databaseDto.getCompiledReport();
        configType = databaseDto.getConfigType();
    }

    /**
     * Updates DB DTO to match user parameters defined by user.
     *
     * @param databaseDto
     *            DB DTO, that contain copy of user defined data.
     */
    public void copyToDatabaseDto(ReportDefinition databaseDto) {
        databaseDto.setName(name);
        databaseDto.setConfigType(configType);
        databaseDto.setCompiledReport(compiledReport);
        if (databaseDto.getParameters() != null) {
            databaseDto.getParameters().clear();
        } else {
            databaseDto.setParameters(new ArrayList<ReportParameter>());
        }
        if (parameters != null) {
            for (ReportAdminParameterEditModel editedParameter : parameters) {
                databaseDto.getParameters().add(editedParameter.getDatabaseDto());
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ReportAdminParameterEditModel> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportAdminParameterEditModel> parameters) {
        this.parameters = parameters;
    }

    public byte[] getCompiledReport() {
        return compiledReport;
    }

    public void setCompiledReport(byte[] compiledReport) {
        this.compiledReport = compiledReport;
    }

    public ReportConfigurationType getConfigType() {
        return configType;
    }

    public void setConfigType(ReportConfigurationType configType) {
        this.configType = configType;
    }

    public boolean hasCompiledReport() {
        return compiledReport != null && compiledReport.length > 0;
    }
}
