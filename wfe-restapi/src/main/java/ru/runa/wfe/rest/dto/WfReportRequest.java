package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class WfReportRequest {
    private String format = "HTML_EMBEDDED";
    private List<WfReportParameterData> parameters = new ArrayList<WfReportParameterData>();
    
    @Data
    public static class WfReportParameterData {
        private String internalName;
        private Object value;
    }
}
