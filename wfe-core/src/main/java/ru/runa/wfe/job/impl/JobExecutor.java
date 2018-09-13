package ru.runa.wfe.job.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.ParsedProcessDefinition;

public class JobExecutor {
    protected final Log log = LogFactory.getLog(getClass());

    @Autowired
    private JobDAO jobDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    @Transactional
    public void execute() {
        List<Job> jobs = jobDAO.getExpiredJobs();
        log.debug("Expired jobs: " + jobs.size());
        for (Job job : jobs) {
            try {
                log.debug("executing " + job);
                ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(job.getProcess().getDeploymentVersion().getId());
                ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, job.getToken());
                job.execute(executionContext);
            } catch (Exception e) {
                log.error("Error executing job " + job, e);
            }
        }
    }
}
