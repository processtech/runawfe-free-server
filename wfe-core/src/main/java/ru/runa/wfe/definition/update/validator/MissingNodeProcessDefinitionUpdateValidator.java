package ru.runa.wfe.definition.update.validator;

import ru.runa.wfe.definition.update.ProcessDefinitionUpdateData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;

/**
 * Checks that all nodes with tokens in active processes exist in new process definition.
 *
 * @author azyablin
 */
@Component
public class MissingNodeProcessDefinitionUpdateValidator implements ProcessDefinitionUpdateValidator {
    @Autowired
    TokenDao tokenDao;

    @Override
    public void validate(ProcessDefinitionUpdateData processDefinitionUpdateData) {
        for (ru.runa.wfe.execution.Process process : processDefinitionUpdateData.getProcesses()) {
            for (Token token : tokenDao.findByProcessAndExecutionStatusIsNotEnded(process)) {
                if (processDefinitionUpdateData.getNewDefinition().getNode(token.getNodeId()) == null) {
                    throw new ProcessDefinitionNotCompatibleException(ProcessDefinitionNotCompatibleException.NODE_EXISTENCE, 
                            new String[] { token.getNodeId(), token.getProcess().getId().toString() });
                }
            }
        }
    }

}
