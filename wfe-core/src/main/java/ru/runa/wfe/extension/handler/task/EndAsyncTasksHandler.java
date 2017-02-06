package ru.runa.wfe.extension.handler.task;

import java.util.List;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.handler.ParamBasedHandlerActionHandler;
import ru.runa.wfe.lang.BaseTaskNode;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class EndAsyncTasksHandler extends ParamBasedHandlerActionHandler {

    @Override
    public void execute(ExecutionContext context) throws Exception {
        String nodeId = paramsDef.getInputParamValueNotNull("nodeId", context.getVariableProvider());
        BaseTaskNode taskNode = (BaseTaskNode) context.getProcessDefinition().getNodeNotNull(nodeId);
        if (!taskNode.isAsync()) {
            throw new IllegalArgumentException("This handler can end only async tasks");
        }
        List<Task> tasks = Lists.newArrayList();
        for (Task task : context.getProcess().getTasks()) {
            if (Objects.equal(nodeId, task.getNodeId())) {
                tasks.add(task);
            }
        }
        log.info("Cancelling tasks by '" + nodeId + "': " + tasks);
        for (Task task : tasks) {
            task.end(context, TaskCompletionInfo.createForHandler(nodeId));
        }
    }

}
