package ru.runa.wfe.job.impl;

import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDao;
import ru.runa.wfe.lang.ProcessDefinition;

@CommonsLog
public class JobExecutor {

    @Autowired
    private JobDao jobDao;
    @Autowired
    private ProcessDefinitionLoader processDefinitionLoader;

    @Transactional
    public void execute() {
        List<Job> jobs = jobDao.getExpiredJobs();
        log.debug("Expired jobs: " + jobs.size());
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
