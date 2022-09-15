package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;

public class EmbeddedSubprocessLikeGraphPartEndNode extends EmbeddedSubprocessEndNode {
    private static final long serialVersionUID = 1L;

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
        executionContext.getToken().end(executionContext.getProcessDefinition(), null, null, false);
        // continue in token from EmbeddedSubprocessStartNode
        leave(new ExecutionContext(executionContext.getProcessDefinition(), enterToken));
    }


}
