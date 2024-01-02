package ru.runa.wfe.definition.update.validator;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.definition.update.ProcessDefinitionUpdateData;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.dao.CurrentTokenDao;

/**
 * Checks that all nodes with tokens in active processes exist in new process definition.
 *
 * @author azyablin
 */
@Component
public class MissingNodeProcessDefinitionUpdateValidator implements ProcessDefinitionUpdateValidator {
    @Autowired
    CurrentTokenDao currentTokenDao;

    @Override
    public void validate(ProcessDefinitionUpdateData processDefinitionUpdateData) {
        if (processDefinitionUpdateData.inBatchMode()) {
            List<String> activeNodeIds = currentTokenDao.findNodeIdsByProcessDefinitionIdAndExecutionStatusIsNotEnded(processDefinitionUpdateData
                    .getOldDefinition().getId());
            for (String nodeId : activeNodeIds) {
                if (processDefinitionUpdateData.getNewDefinition().getNode(nodeId) == null) {
                    throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.NODE_EXISTENCE, new String[] { nodeId });
                }
            }
        } else {
            for (CurrentToken token : currentTokenDao.findByProcessAndExecutionStatusIsNotEnded(processDefinitionUpdateData.getProcess().get())) {
                if (processDefinitionUpdateData.getNewDefinition().getNode(token.getNodeId()) == null) {
                    throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.NODE_EXISTENCE, new String[] {
                            token.getNodeId(), token.getProcess().getId().toString() });
                }
            }
        }
    }

}
