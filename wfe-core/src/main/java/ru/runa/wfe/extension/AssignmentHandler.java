package ru.runa.wfe.extension;

import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.Executor;

/**
 * assigns {@link ru.runa.wfe.task.Task}s or {@link CurrentSwimlane}s to actors.
 */
public interface AssignmentHandler extends Configurable {

    /**
     * assigns the assignable (={@link ru.runa.wfe.task.Task} or a {@link CurrentSwimlane} to an swimlaneActorId.
     * <p>
     * The swimlaneActorId is the user that is responsible for the given task or swimlane.
     * The pooledActors represents a pool of actors to which the task or swimlane is offered.
     * Any actors from the pool can then take a Task by calling {@link ru.runa.wfe.task.Task#setExecutor(Executor)}.
     */
    void assign(ExecutionContext executionContext, Assignable assignable);
}
