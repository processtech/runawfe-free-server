package ru.runa.wfe.job.impl;

import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

@CommonsLog
public class JobExecutor {

    @Autowired
    private JobTransactionalExecutor jobTransactionalExecutor;

    @Scheduled(fixedDelayString = "${timertask.period.millis.job.execution}")
    public void execute() {
        for (Long jobId : jobTransactionalExecutor.getExpiredJobIds()) {
            try {
                log.debug("executing job " + jobId);
                jobTransactionalExecutor.execute(jobId);
            } catch (Exception e) {
                log.error("Error executing job " + jobId, e);
                jobTransactionalExecutor.onExecutionFailed(jobId, e);
            }
        }
    }
}
