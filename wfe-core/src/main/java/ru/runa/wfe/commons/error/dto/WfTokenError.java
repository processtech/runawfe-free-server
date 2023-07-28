package ru.runa.wfe.commons.error.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ParsedProcessDefinition;
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
    private String stackTrace;

    public WfTokenError(Token token, String stackTrace) {
        this.id = token.getId();
        this.processId = token.getProcess().getId();
        ParsedProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader().getDefinition(token.getProcess());
        this.processType = processDefinition.getCategory();
        this.processName = processDefinition.getName();
        this.processVersion = processDefinition.getVersion();
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
