package ru.runa.wfe.commons.error.dto;

import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WfTokenError extends SecuredObjectBase {
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
        ProcessDefinition processDefinition = ApplicationContextFactory.getProcessDefinitionLoader()
                .getDefinition(token.getProcess().getDeployment().getId());
        this.processType = processDefinition.getDeployment().getCategory();
        this.processName = processDefinition.getName();
        this.processVersion = processDefinition.getDeployment().getVersion();
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
