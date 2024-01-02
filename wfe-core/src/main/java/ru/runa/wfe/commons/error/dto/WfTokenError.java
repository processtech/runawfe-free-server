package ru.runa.wfe.commons.error.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WfTokenError extends SecuredObject {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long processId;
    private String processType;
    private String processName;
    private Long processVersion;
    private ExecutionStatus processExecutionStatus;
    private String nodeId;
    private String nodeName;
    private NodeType nodeType;
    private Date nodeEnterDate;
    private Date errorDate;
    private String errorMessage;


    public WfTokenError(Token token) {
        this.id = token.getId();
        this.processId = token.getProcess().getId();
        this.processType = token.getProcess().getDefinition().getPack().getCategory();
        this.processName = token.getProcess().getDefinition().getPack().getName();
        this.processVersion = token.getProcess().getDefinition().getVersion();
        this.processExecutionStatus = token.getProcess().getExecutionStatus();
        this.nodeId = token.getNodeId();
        this.nodeName = token.getNodeName();
        this.nodeType = token.getNodeType();
        this.nodeEnterDate = token.getNodeEnterDate();
        this.errorDate = token.getErrorDate();
        this.errorMessage = token.getErrorMessage();
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.ERRORS;
    }
}
