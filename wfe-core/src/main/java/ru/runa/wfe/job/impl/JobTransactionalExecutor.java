package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;
import ru.runa.wfe.lang.ProcessDefinition;

public class JobTransactionalExecutor extends TransactionalExecutor {
    @Autowired
    private JobDAO jobDAO;
    @Autowired
    private IProcessDefinitionLoader processDefinitionLoader;

    private Long jobId;

    public void setJobId(Long id) {
        this.jobId = id;
    }

    @Override
    protected void doExecuteInTransaction() {
        Job job = null;
        try {
            job = jobDAO.getNotNull(jobId);
            log.debug("executing " + job);
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(job.getProcess().getDeployment().getId());
            ExecutionContext executionContext = new ExecutionContext(processDefinition, job.getToken());
            job.execute(executionContext);
        } catch (Exception e) {
            // for rollback
            throw new InternalApplicationException("Error execute job " + job, e);
        }
    }
}
