package ru.runa.wfe.service;

import javax.ejb.Remote;

import ru.runa.wfe.user.User;

/**
 * Service for internal system startup.
 * 
 * @author dofs
 * @since 4.0.4
 */
@Remote
public interface SynchronizationService {

    /**
     * Synchronizes executors with LDAP, if configured.
     * 
     * @param user
     *            authorized user
     */
    public void synchronizeExecutorsWithLDAP(User user, boolean createExecutors);

}
