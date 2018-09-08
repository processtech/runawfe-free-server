package ru.runa.wfe.job.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.security.logic.LdapLogic;

public class LdapSynchronizer {
    @Autowired
    private LdapLogic ldapLogic;

    @Transactional
    public void execute() {
        ldapLogic.synchronizeExecutors();
    }
}
