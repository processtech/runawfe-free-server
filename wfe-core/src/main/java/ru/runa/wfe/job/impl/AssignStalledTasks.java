package ru.runa.wfe.job.impl;

import org.springframework.stereotype.Component;

@Component
public class AssignStalledTasks extends JobTask<AssignStalledTasksExecutor> {

    @Override
    protected void execute() throws Exception {
        getTransactionalExecutor().executeInTransaction(false);
    }

}
