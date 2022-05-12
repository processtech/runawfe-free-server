package ru.runa.wfe.job.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.ProcessDefinition;

public class JobExecutor {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private JobDao jobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    @Transactional
    public void execute() {
        Long batchSize = SystemProperties.getJobExecutorBatchSize();
        Long expiredJobsCount = jobDao.getExpiredJobsCount();
        List<Job> jobs = jobDao.getExpiredJobs(batchSize);
        log.debug("Expired jobs: " + expiredJobsCount);
        if (expiredJobsCount > batchSize) {
            log.debug("Too many expired jobs. Processing first " + batchSize);
        }
        for (Job job : jobs) {
            try {
                log.debug("executing " + job);
                ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(job.getProcess().getDeployment().getId());
                ExecutionContext executionContext = new ExecutionContext(processDefinition, job.getToken());
                job.execute(executionContext);
            } catch (Exception e) {
                log.error("Error executing job " + job, e);
            }
        }
    }

}
