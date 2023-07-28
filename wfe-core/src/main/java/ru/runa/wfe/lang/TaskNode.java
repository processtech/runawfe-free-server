package ru.runa.wfe.lang;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.CurrentSwimlane;

/**
 * is a node that relates to one or more tasks. Property <code>signal</code> specifies how task completion triggers continuation of execution.
 */
public class TaskNode extends BaseTaskNode {
    private static final long serialVersionUID = 1L;

    @Override
    public NodeType getNodeType() {
        return NodeType.TASK_STATE;
    }

    @Override
    protected void execute(ExecutionContext executionContext) throws Exception {
        for (TaskDefinition taskDefinition : taskDefinitions) {
            CurrentSwimlane swimlane = getInitializedSwimlaneNotNull(executionContext, taskDefinition);
            // copy the swimlane assignment into the task
            taskFactory.create(executionContext, executionContext.getVariableProvider(), taskDefinition, swimlane, swimlane.getExecutor(), null, async);
        }
        if (async) {
            log.debug("continue execution in async " + this);
            leave(executionContext);
        }
    }

}
