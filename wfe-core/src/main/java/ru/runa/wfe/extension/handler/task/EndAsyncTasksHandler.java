package ru.runa.wfe.extension.handler.task;

import java.util.ArrayList;
import lombok.val;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.ParamBasedHandlerActionHandler;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

public class EndAsyncTasksHandler extends ParamBasedHandlerActionHandler {

    @Override
    public void execute(ExecutionContext context) throws Exception {
        String nodeId = paramsDef.getInputParamValueNotNull("nodeId", context.getVariableProvider());
        BaseTaskNode taskNode = (BaseTaskNode) context.getProcessDefinition().getNodeNotNull(nodeId);
        if (!taskNode.isAsync()) {
            throw new IllegalArgumentException("This handler can end only async tasks");
        }
        val tasks = new ArrayList<Task>(ApplicationContextFactory.getTaskDao().findByProcessAndNodeId(context.getCurrentProcess(), nodeId));
        log.info("Cancelling tasks by '" + nodeId + "': " + tasks);
        for (Task task : tasks) {
            task.end(context, taskNode, TaskCompletionInfo.createForHandler(nodeId));
        }
    }
}
