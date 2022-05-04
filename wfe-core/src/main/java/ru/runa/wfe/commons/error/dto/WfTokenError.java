package ru.runa.wfe.commons.error.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WfTokenError extends SecuredObjectBase {
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
    private Date errorDate;
    private String errorMessage;
    private String stackTrace;

    public WfTokenError(Token token, String stackTrace) {
        this.id = token.getId();
        this.processId = token.getProcess().getId();
        this.processName = token.getProcess().getDeployment().getName();
        this.processVersion = token.getProcess().getDeployment().getVersion();
        this.processExecutionStatus = token.getProcess().getExecutionStatus();
        this.nodeId = token.getNodeId();
        this.nodeName = token.getNodeName();
        this.nodeType = token.getNodeType();
        this.nodeEnterDate = token.getNodeEnterDate();
        this.errorDate = token.getErrorDate();
        this.errorMessage = token.getErrorMessage();
        this.stackTrace = stackTrace;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.ERRORS;
    }
}
