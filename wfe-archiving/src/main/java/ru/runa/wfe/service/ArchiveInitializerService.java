package ru.runa.wfe.service;

import javax.ejb.Remote;

@Remote
public interface ArchiveInitializerService {

    /**
     * Invoked on application start from application server.
     */
    public void onSystemStartup();
}
