package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.report.ReportParameterType;

@Data
public class WfeReportParameter {
    private String userName;
    private String description;
    private String internalName;
    private int position;
    private ReportParameterType type;
    private boolean isRequired;
}
