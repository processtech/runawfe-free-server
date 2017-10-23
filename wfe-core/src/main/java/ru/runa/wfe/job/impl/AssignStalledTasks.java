package ru.runa.wfe.job.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.task.logic.TaskAssigner;

public class AssignStalledTasks extends JobTask<TransactionalExecutor> {
    @Autowired
    private TaskAssigner taskAssigner;
    @Autowired
    private TaskDAO taskDAO;

    @Override
    protected void execute() throws Exception {
        List<Task> unassignedTasks = taskDAO.findUnassignedTasksInActiveProcesses();
        log.debug("Unassigned tasks: " + unassignedTasks.size());
        for (Task unassignedTask : unassignedTasks) {
            taskAssigner.assignTask(unassignedTask);
        }
    }

}
