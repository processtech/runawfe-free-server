package ru.runa.wfe.audit;

import com.google.common.collect.Lists;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class ProcessLogFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    private ProcessLog.Type type = CurrentProcessLog.Type.ALL;
    private Long idFrom;
    private Long idTo;
    private Date createDateFrom;
    private Date createDateTo;
    private Long processId;
    private Long tokenId;
    private String nodeId;
    private boolean includeSubprocessLogs;
    private List<Severity> severities = Lists.newArrayList();

    public ProcessLogFilter() {
    }

    public ProcessLogFilter(Long processId) {
        this.processId = processId;
    }

    public ProcessLogFilter(ProcessLogFilter filter) {
        this.type = filter.type;
        this.idFrom = filter.idFrom;
        this.idTo = filter.idTo;
        this.createDateFrom = filter.createDateFrom;
        this.createDateTo = filter.createDateTo;
        this.processId = filter.processId;
        this.tokenId = filter.tokenId;
        this.nodeId = filter.nodeId;
        this.includeSubprocessLogs = filter.includeSubprocessLogs;
        this.severities = filter.severities;
    }

    public ProcessLog.Type getType() {
        return type;
    }

    public void setType(ProcessLog.Type type) {
        this.type = type;
    }

    public Long getIdFrom() {
        return idFrom;
    }

    public void setIdFrom(Long idFrom) {
        this.idFrom = idFrom;
    }

    public Long getIdTo() {
        return idTo;
    }

    public void setIdTo(Long idTo) {
        this.idTo = idTo;
    }

    public Date getCreateDateFrom() {
        return createDateFrom;
    }

    public void setCreateDateFrom(Date createDateFrom) {
        this.createDateFrom = createDateFrom;
    }

    public Date getCreateDateTo() {
        return createDateTo;
    }

    public void setCreateDateTo(Date createDateTo) {
        this.createDateTo = createDateTo;
    }

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public boolean isIncludeSubprocessLogs() {
        return includeSubprocessLogs;
    }

    public void setIncludeSubprocessLogs(boolean includeSubprocessLogs) {
        this.includeSubprocessLogs = includeSubprocessLogs;
    }

    public List<Severity> getSeverities() {
        return severities;
    }

    public void addSeverity(Severity severity) {
        this.severities.add(severity);
    }

    public void setSeverities(List<Severity> severities) {
        this.severities = severities;
    }

}
