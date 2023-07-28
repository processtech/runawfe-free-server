package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
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
    @Scheduled(fixedDelayString = "${timertask.period.millis.unassigned.tasks.execution}")
    public void execute() {
        List<Task> unassignedTasks = taskDao.findUnassignedTasksInActiveProcesses();
        log.debug("Unassigned tasks: " + unassignedTasks.size());
        for (Task unassignedTask : unassignedTasks) {
            taskAssigner.assignTask(unassignedTask);
        }
    }

}
