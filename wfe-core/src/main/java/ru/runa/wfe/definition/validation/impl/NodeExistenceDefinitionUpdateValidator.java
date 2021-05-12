package ru.runa.wfe.definition.validation.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.validation.DefinitionUpdateValidator;
import ru.runa.wfe.definition.validation.DeploymentUpdateData;
import ru.runa.wfe.definition.validation.ProcessDefinitionNotCompatibleException;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;

/**
 * Checks that all nodes with tokens in active processes exist in new process definition.
 *
 * @author azyablin
 */
@Component
public class NodeExistenceDefinitionUpdateValidator implements DefinitionUpdateValidator {
    @Autowired
    TokenDao tokenDao;

    @Override
    public void validate(DeploymentUpdateData deploymentUpdateData) {
        for (ru.runa.wfe.execution.Process process : deploymentUpdateData.getProcesses()) {
            for (Token token : tokenDao.findByProcessAndExecutionStatusIsNotEnded(process)) {
                String nodeId = token.getNodeId();
                if (deploymentUpdateData.getNewDefinition().getNode(nodeId) == null) {
                    throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.NODE_EXISTENCE, 
                            new String[] { nodeId, token.getProcess().getId().toString() });
                }
            }
        }
    }

}
