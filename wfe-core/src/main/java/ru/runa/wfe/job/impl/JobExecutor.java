package ru.runa.wfe.job.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class JobExecutor {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private JobTransactionalExecutor jobTransactionalExecutor;

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
