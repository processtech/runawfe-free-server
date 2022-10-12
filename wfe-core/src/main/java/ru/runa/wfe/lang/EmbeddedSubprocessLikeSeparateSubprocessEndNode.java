package ru.runa.wfe.lang;

import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.task.TaskCompletionInfo;

public class EmbeddedSubprocessLikeSeparateSubprocessEndNode extends EmbeddedSubprocessEndNode {
    private static final long serialVersionUID = 1L;
    private boolean endToken;
    @Autowired
    private transient ExecutionLogic executionLogic;

    public boolean isEndToken() {
        return endToken;
    }

    public void setEndToken(boolean endToken) {
        this.endToken = endToken;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        CurrentToken enterToken = executionContext.getCurrentToken();
        while (!enterToken.getNodeId().equals(subprocessNode.getNodeId())) {
            enterToken = enterToken.getParent();
            if (enterToken == null) {
                throw new InternalApplicationException("No corresponding token found for embedded subprocess end");
            }
        }
        if (endToken) {
            executionLogic.endToken(executionContext.getCurrentToken(), executionContext.getParsedProcessDefinition(), null, null, false);
            if (!enterToken.hasActiveChild()) {
                leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), enterToken));
            }
        } else {
            TaskCompletionInfo taskCompletionInfo = TaskCompletionInfo.createForEmbeddedSubprocessEnd();
            for (CurrentToken child : enterToken.getChildren()) {
                executionLogic.endToken(child, executionContext.getParsedProcessDefinition(), null, taskCompletionInfo, true);
            }
            leave(new ExecutionContext(executionContext.getParsedProcessDefinition(), enterToken));
        }
    }

}
