package ru.runa.wfe.job.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.job.Job;
import ru.runa.wfe.job.dao.JobDAO;

public class ExpiredJobCheckerTask extends JobTask<JobTransactionalExecutor> {
    @Autowired
    private JobDAO jobDAO;

    @Override
    protected void execute() throws Exception {
        List<Job> jobs = jobDAO.getExpiredJobs();
        log.debug("Expired jobs: " + jobs.size());
        for (Job job : jobs) {
            JobTransactionalExecutor transactionalExecutor = getTransactionalExecutor();
            transactionalExecutor.setJobId(job.getId());
            transactionalExecutor.executeInTransaction(false);
        }
    }

}
