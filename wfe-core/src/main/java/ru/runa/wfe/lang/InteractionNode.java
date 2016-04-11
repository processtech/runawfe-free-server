package ru.runa.wfe.lang;

import java.util.List;

import ru.runa.wfe.InternalApplicationException;

import com.google.common.collect.Lists;

public abstract class InteractionNode extends Node {
    private static final long serialVersionUID = 1L;
    protected List<TaskDefinition> taskDefinitions = Lists.newArrayList();

    public void addTask(TaskDefinition taskDefinition) {
        taskDefinitions.add(taskDefinition);
        taskDefinition.setNode(this);
    }

    public List<TaskDefinition> getTasks() {
        return taskDefinitions;
    }

    public TaskDefinition getFirstTaskNotNull() {
        if (taskDefinitions.size() > 0) {
            return taskDefinitions.get(0);
        }
        throw new InternalApplicationException("There are no tasks in " + this);
    }

    @Override
    public void validate() {
        super.validate();
        for (TaskDefinition taskDefinition : taskDefinitions) {
            taskDefinition.validate();
        }
    }
}
