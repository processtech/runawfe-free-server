package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.audit.TaskCancelledByProcessEndLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.TaskCompletionInfo;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.user.TemporaryGroup;

@CommonsLog
public class AsyncTaskChecker {

    @Autowired
    private TaskDao taskDao;
    @Autowired
    private ProcessLogDao processLogDao;
    private long ttlInMillis;

    public void setTtlInSeconds(long ttlInSeconds) {
        this.ttlInMillis = ttlInSeconds * 1000;
    }

    @Transactional
    public void execute() {
        if (ttlInMillis <= 0) {
            return;
        }
        long curTimeInMillis = System.currentTimeMillis();
        List<Task> tasks = taskDao.findByEndedProcess();
        for (Task task : tasks) {
            long processEndTime = task.getProcess().getEndDate().getTime();
            if (processEndTime < curTimeInMillis - ttlInMillis) {
                log.info("Asynchronous " + task + " time to live exceeded, deleting it");
                processLogDao.addLog(new TaskCancelledByProcessEndLog(task, TaskCompletionInfo.createForProcessEnd(task.getProcess().getId())),
                        task.getProcess(), task.getToken());
                task.delete();
                List<Task> swimlaneTasks = ApplicationContextFactory.getTaskDAO().findByProcessAndSwimlane(task.getProcess(), task.getSwimlane());
                if (swimlaneTasks.isEmpty()) {
                    if (task.getSwimlane().getExecutor() instanceof TemporaryGroup) {
                        task.getSwimlane().setExecutor(null);
                        log.debug("Cleared swimlane temporary group");
                    }
                } else {
                    log.debug("Swimlane temporary group is used in " + swimlaneTasks);
                }
            }
        }
    }
}
