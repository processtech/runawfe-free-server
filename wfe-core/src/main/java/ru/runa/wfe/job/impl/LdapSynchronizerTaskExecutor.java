package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.security.logic.LdapLogic;

public class LdapSynchronizerTaskExecutor extends TransactionalExecutor {
    @Autowired
    private LdapLogic ldapLogic;

    @Override
    protected void doExecuteInTransaction() throws Exception {
        ldapLogic.synchronizeExecutors();
    }

}
