package ru.runa.wfe.job.impl;

public class RemoveUnusedTemporaryGroupsTask extends JobTask<RemoveUnusedTemporaryGroupsExecutor> {

    @Override
    protected void execute() throws Exception {
        getTransactionalExecutor().executeInTransaction(false);
    }

}
