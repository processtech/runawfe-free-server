package ru.runa.wfe.commons.error;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Objects;

@XmlAccessorType(XmlAccessType.FIELD)
public class ProcessError extends SystemError {
    private static final long serialVersionUID = 1L;
    private ProcessErrorType type;
    private Long processId;
    private String nodeId;
    private String nodeName;

    public ProcessError() {
    }

    public ProcessError(ProcessErrorType type, Long processId, String nodeId) {
        super(null);
        this.type = type;
        this.processId = processId;
        this.nodeId = nodeId;
    }

    public ProcessErrorType getType() {
        return type;
    }

    public Long getProcessId() {
        return processId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, processId, nodeId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProcessError) {
            ProcessError o = (ProcessError) obj;
            return Objects.equal(type, o.type) && Objects.equal(processId, o.processId) && Objects.equal(nodeId, o.nodeId);
        }
        return super.equals(obj);
    }
}