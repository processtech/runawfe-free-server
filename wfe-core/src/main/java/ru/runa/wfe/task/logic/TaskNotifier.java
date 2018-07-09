package ru.runa.wfe.task.logic;

import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableProvider;

/**
 * Interface for new tasks notification through third-party application.
 *
 * @author Dofs
 */
public interface TaskNotifier {

    /**
     * Invoked when task assignment changed
     */
    public void onTaskAssigned(ProcessDefinition processDefinition, VariableProvider variableProvider, Task task, Executor previousExecutor);

}
