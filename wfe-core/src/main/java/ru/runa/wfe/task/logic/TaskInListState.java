package ru.runa.wfe.task.logic;

import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

/**
 * State of task, which must be in task list. We use this object for future loading related data (variables and so on) with single database query.
 * 
 * @author AL
 */
public class TaskInListState {

    /**
     * Task for tasks list.
     */
    private final Task task;

    /**
     * Flag, equals true, if task is acquired by substitution and false otherwise.
     */
    private final boolean acquiredBySubstitution;

    /**
     * Actor, working with task.
     */
    private final Actor actor;

    public TaskInListState(Task task, Actor actor, boolean acquiredBySubstitution) {
        super();
        this.task = task;
        this.actor = actor;
        this.acquiredBySubstitution = acquiredBySubstitution;
    }

    public Task getTask() {
        return task;
    }

    public boolean isAcquiredBySubstitution() {
        return acquiredBySubstitution;
    }

    public Actor getActor() {
        return actor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + task.getId().hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TaskInListState other = (TaskInListState) obj;
        return task.getId().equals(other.task.getId());
    }
}