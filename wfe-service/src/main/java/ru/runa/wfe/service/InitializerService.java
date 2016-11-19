package ru.runa.wfe.service;

import javax.ejb.Remote;

/**
 * Service for internal system startup.
 * 
 * @author dofs
 * @since 4.0
 */
@Remote
public interface InitializerService {

    /**
     * Invoked on application start from application server.
     */
    public void onSystemStartup();

}
