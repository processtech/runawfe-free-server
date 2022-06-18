package ru.runa.wfe.rest.dto;

import lombok.Data;

@Data
public class WfeReportResult {
    private String message;
    private String reportFileName;
    private Object reportData;
}
