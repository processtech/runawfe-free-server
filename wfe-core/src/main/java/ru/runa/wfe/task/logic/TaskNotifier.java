package ru.runa.wfe.task.logic;

import ru.runa.wfe.lang.ParsedProcessDefinition;
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
    void onTaskAssigned(ParsedProcessDefinition parsedProcessDefinition, VariableProvider variableProvider, Task task, Executor previousExecutor);
}
