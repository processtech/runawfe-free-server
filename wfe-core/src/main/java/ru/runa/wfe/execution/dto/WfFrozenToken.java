package ru.runa.wfe.execution.dto;

import java.util.Date;
import java.util.Objects;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;

@Data
@NoArgsConstructor
public class WfFrozenToken extends SecuredObjectBase {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long processId;
    private String processName;
    private Long processVersion;
    private ExecutionStatus processExecutionStatus;
    private String nodeId;
    private String nodeName;
    private NodeType nodeType;
    private Date nodeEnterDate;
    private String typeName;
    private String additionalInfo;

    public WfFrozenToken(Token token, String typeName) {
        this.id = token.getId();
        this.processId = token.getProcess().getId();
        this.processName = token.getProcess().getDeployment().getName();
        this.processVersion = token.getProcess().getDeployment().getVersion();
        this.processExecutionStatus = token.getProcess().getExecutionStatus();
        this.nodeId = token.getNodeId();
        this.nodeName = token.getNodeName();
        this.nodeType = token.getNodeType();
        this.nodeEnterDate = token.getNodeEnterDate();
        this.typeName = typeName;
        this.additionalInfo = "";
    }

    public WfFrozenToken(Token token, String typeName, String additionalInfo) {
        this.id = token.getId();
        this.processId = token.getProcess().getId();
        this.processName = token.getProcess().getDeployment().getName();
        this.processVersion = token.getProcess().getDeployment().getVersion();
        this.processExecutionStatus = token.getProcess().getExecutionStatus();
        this.nodeId = token.getNodeId();
        this.nodeName = token.getNodeName();
        this.nodeType = token.getNodeType();
        this.nodeEnterDate = token.getNodeEnterDate();
        this.typeName = typeName;
        this.additionalInfo = additionalInfo;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.FROZEN_PROCESSES;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WfFrozenToken other = (WfFrozenToken) obj;
        return Objects.equals(id, other.id) && Objects.equals(typeName, other.typeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typeName);
    }

}
