package ru.runa.wfe.lang;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.task.TaskCompletionInfo;

public class EmbeddedSubprocessLikeSeparateSubprocessEndNode extends EmbeddedSubprocessEndNode {
    private static final long serialVersionUID = 1L;
    private boolean endToken;

    public boolean isEndToken() {
        return endToken;
    }

    public void setEndToken(boolean endToken) {
        this.endToken = endToken;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        Token enterToken = executionContext.getToken();
        while (!enterToken.getNodeId().equals(subprocessNode.getNodeId())) {
            enterToken = enterToken.getParent();
            if (enterToken == null) {
                throw new InternalApplicationException("No corresponding token found for embedded subprocess end");
            }
        }
        if (endToken) {
            executionContext.getToken().end(executionContext.getProcessDefinition(), null, null, false);
            if (!enterToken.hasActiveChild()) {
                leave(new ExecutionContext(executionContext.getProcessDefinition(), enterToken));
            }
        } else {
            TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForEmbeddedSubprocessEnd();
            for (Token child : enterToken.getChildren()) {
                child.end(executionContext.getProcessDefinition(), null, taskCompletionInfo, true);
            }
            leave(new ExecutionContext(executionContext.getProcessDefinition(), enterToken));
        }
    }

}
