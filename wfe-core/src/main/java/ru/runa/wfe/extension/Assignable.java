package ru.runa.wfe.extension;

import java.io.Serializable;

import ru.runa.wfe.execution.CurrentSwimlane;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.user.Executor;

/**
 * Common superclass for {@link ru.runa.wfe.task.Task}s and {@link CurrentSwimlane}s used by the
 * {@link ru.runa.wfe.extension.AssignmentHandler} interface.
 */
public interface Assignable extends Serializable {

    String getName();

    String getSwimlaneName();

    /**
     * sets the responsible for this assignable object. Use this method to assign the task into a user's personal task list.
     * 
     * @param cascadeUpdate
     *            for task: update swimlane; for swimlane: update tasks
     */
    void assignExecutor(ExecutionContext executionContext, Executor executor, boolean cascadeUpdate);

    /**
     * @return currently assigned executor
     */
    Executor getExecutor();
}
