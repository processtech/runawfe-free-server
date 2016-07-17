package ru.runa.wfe.execution.async;

import ru.runa.wfe.commons.Utils;

/**
 * 
 * @author Alex Chernyshev
 */
public class JMSNodeAsyncExecutor implements INodeAsyncExecutor {

    @Override
    public void execute(Long processId, Long tokenId) {
        Utils.sendNodeAsyncExecutionMessage(processId, tokenId);
    }

}