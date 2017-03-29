package ru.runa.wfe.job.impl;

public class LdapSynchronizerTask extends JobTask<LdapSynchronizerTaskExecutor> {

    @Override
    protected void execute() throws Exception {
        getTransactionalExecutor().executeInTransaction(false);
    }
}
