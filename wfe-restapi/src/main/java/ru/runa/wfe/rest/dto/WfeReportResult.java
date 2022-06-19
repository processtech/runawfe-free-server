package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class WfeReportResult {
    private List<String> errorParameterNames = new ArrayList<>();
    private String reportFileName;
    private Object reportData;
}
