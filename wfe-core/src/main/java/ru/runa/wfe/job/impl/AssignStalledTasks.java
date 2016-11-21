package ru.runa.wfe.job.impl;


public class AssignStalledTasks extends JobTask<AssignStalledTasksExecutor> {

    @Override
    protected void execute() throws Exception {
        getTransactionalExecutor().executeInTransaction(false);
    }

}
