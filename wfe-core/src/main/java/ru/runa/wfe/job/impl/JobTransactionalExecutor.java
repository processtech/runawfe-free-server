package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.TimerJob;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@Component
@Transactional
@CommonsLog
public class JobTransactionalExecutor {
    @Autowired
    private JobDao jobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;
    @Autowired
    private ExecutionLogic executionLogic;

    public List<TimerJob> getExpiredJobs() {
        Long batchSize = SystemProperties.getJobExecutorBatchSize();
        Long expiredJobsCount = jobDao.getExpiredJobsCount();
        List<TimerJob> jobs = jobDao.getExpiredJobs(batchSize);
        log.debug("Expired jobs: " + expiredJobsCount);
        if (expiredJobsCount > batchSize) {
            log.debug("Too many expired jobs. Processing first " + batchSize);
        }
        return jobs;
    }

    public void execute(Long jobId) {
        Job job = jobDao.get(jobId);
        ParsedProcessDefinition parsed = processDefinitionLoader.getDefinition(job.getProcess());
        ExecutionContext executionContext = new ExecutionContext(parsed, job.getToken());
        job.execute(executionContext);
    }

    public void onExecutionFailed(Long jobId, Exception e) {
        Job job = jobDao.get(jobId);
        executionLogic.failToken(job.getToken(), e);
    }
}
