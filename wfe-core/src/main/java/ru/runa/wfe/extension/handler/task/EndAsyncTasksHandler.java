package ru.runa.wfe.extension.handler.task;

import com.google.common.collect.Lists;
import java.util.List;
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
        BaseTaskNode taskNode = (BaseTaskNode) context.getParsedProcessDefinition().getNodeNotNull(nodeId);
        if (!taskNode.isAsync()) {
            throw new IllegalArgumentException("This handler can end only async tasks");
        }
        List<Task> tasks = Lists.newArrayList();
        for (Task task : ApplicationContextFactory.getTaskDAO().findByProcessAndNodeId(context.getProcess(), nodeId)) {
            tasks.add(task);
        }
        log.info("Cancelling tasks by '" + nodeId + "': " + tasks);
        for (Task task : tasks) {
            task.end(context, taskNode, TaskCompletionInfo.createForHandler(nodeId));
        }
    }

}
