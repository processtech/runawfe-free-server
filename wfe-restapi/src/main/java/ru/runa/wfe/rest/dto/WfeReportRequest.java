package ru.runa.wfe.rest.dto;

import java.util.List;
import lombok.Data;

@Data
public class WfeReportRequest {
    private String format = "HTML_EMBEDDED";
    private List<WfeReportParameter> parameters;
    
    @Data
    public static class WfeReportParameter {
        private String internalName;
        private Object value;
    }
}
