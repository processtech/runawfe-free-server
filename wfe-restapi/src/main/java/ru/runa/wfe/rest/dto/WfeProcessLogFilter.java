package ru.runa.wfe.rest.dto;

import java.util.Date;
import lombok.Data;
import ru.runa.wfe.audit.ProcessLog;

@Data
public class WfeProcessLogFilter {
    private ProcessLog.Type type = ProcessLog.Type.ALL;
    private Long idFrom;
    private Long idTo;
    private Date createDateFrom;
    private Date createDateTo;
    private Long tokenId;
    private String nodeId;
    private boolean includeSubprocessLogs;

}
