package ru.runa.wfe.rest.dto;

import java.util.Date;
import java.util.Map;
import lombok.Data;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.Severity;

@Data
public class WfeProcessLog {
    private Long id;
    private Long processId;
    private Long tokenId;
    private String nodeId;
    private String nodeName;
    private String executorName;
    private String swimlaneName;
    private String variableName;
    private Long taskId;
    private Date createDate;
    private Severity severity;
    private ProcessLog.Type type;
    private Map<String, String> attributes;
}
