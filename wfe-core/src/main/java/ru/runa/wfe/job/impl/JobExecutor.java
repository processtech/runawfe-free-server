package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.ParsedProcessDefinition;

@CommonsLog
public class JobExecutor {

    @Autowired
    private JobDao jobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    @Transactional
    @Scheduled(fixedDelayString = "${timertask.period.millis.job.execution}")
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
                ParsedProcessDefinition parsed = processDefinitionLoader.getDefinition(job.getProcess().getDefinitionVersion().getId());
                ExecutionContext executionContext = new ExecutionContext(parsed, job.getToken());
                job.execute(executionContext);
            } catch (Exception e) {
                log.error("Error executing job " + job, e);
            }
        }
    }
}
