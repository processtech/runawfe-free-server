package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TransactionalExecutor;

public class ExpiredTasksNotifierJob extends TransactionalExecutor {

    @Autowired
    private ExpiredTasksNotifierJobExecutor expiredTasksNotifierJobExecutor;

    @Override
    protected void doExecuteInTransaction() throws Exception {
        expiredTasksNotifierJobExecutor.execute();
    }

}
