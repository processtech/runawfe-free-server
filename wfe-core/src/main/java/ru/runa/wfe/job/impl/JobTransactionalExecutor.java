package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.ProcessDefinition;

@Component
@Transactional
@CommonsLog
public class JobTransactionalExecutor {
    @Autowired
    private JobDao jobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    public List<Long> getExpiredJobIds() {
        Long batchSize = SystemProperties.getJobExecutorBatchSize();
        Long expiredJobsCount = jobDao.getExpiredJobsCount();
        List<Long> jobIds = jobDao.getExpiredJobIds(batchSize);
        log.debug("Expired jobs: " + expiredJobsCount);
        if (expiredJobsCount > batchSize) {
            log.debug("Too many expired jobs. Processing first " + batchSize);
        }
        return jobIds;
    }

    public void execute(Long jobId) {
        Job job = jobDao.get(jobId);
        ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(job.getProcess().getDeployment().getId());
        ExecutionContext executionContext = new ExecutionContext(processDefinition, job.getToken());
        job.execute(executionContext);
    }

    public void onExecutionFailed(Long jobId, Exception e) {
        Job job = jobDao.get(jobId);
        job.getToken().fail(e);
    }
}
