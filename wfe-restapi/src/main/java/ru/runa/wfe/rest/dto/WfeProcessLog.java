package ru.runa.wfe.rest.dto;

import lombok.Data;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.Severity;
import java.util.Date;
import java.util.Map;

@Data
public class WfeProcessLog {
    private Long id;
    private Long processId;
    private Long tokenId;
    private String nodeId;
    private Date createDate;
    private Severity severity;
    private ProcessLog.Type type;
    private Map<String, String> attributes;
}
