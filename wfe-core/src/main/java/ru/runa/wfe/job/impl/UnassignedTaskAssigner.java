package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.logic.TaskAssigner;

@CommonsLog
public class UnassignedTaskAssigner {

    @Autowired
    private TaskAssigner taskAssigner;
    @Autowired
    private TaskDao taskDao;

    @Transactional
    public void execute() {
        if (!ApplicationContextFactory.getInitializerLogic().isInitialized()) {
            // Do not interfere with migrations.
            return;
        }

        List<Task> unassignedTasks = taskDao.findUnassignedTasksInActiveProcesses();
        log.debug("Unassigned tasks: " + unassignedTasks.size());
        for (Task unassignedTask : unassignedTasks) {
            taskAssigner.assignTask(unassignedTask);
        }
    }

}
