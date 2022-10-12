package ru.runa.wfe.lang;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.logic.ExecutionLogic;

public class EmbeddedSubprocessLikeGraphPartEndNode extends EmbeddedSubprocessEndNode {
    private static final long serialVersionUID = 1L;
    @Autowired
    private transient ExecutionLogic executionLogic;
    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        Token enterToken = executionContext.getToken();
        while (!enterToken.getNodeId().equals(subprocessNode.getNodeId())) {
            enterToken = enterToken.getParent();
            if (enterToken == null) {
                // uncomment in future version
                // throw new InternalApplicationException("No corresponding token found for embedded subprocess end");
                log.warn("No corresponding token found for embedded subprocess end; providing backwards compatibility behavior");
                leave(executionContext);
                return;
            }
        }
        executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null, null, false);
        // continue in token from EmbeddedSubprocessStartNode
        leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), enterToken));
    }


}
