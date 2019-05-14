package ru.runa.wfe.service;

import java.util.List;

import ru.runa.wfe.user.User;

public interface DataSourceService {

    List<String> getNames();

    void importDataSource(User user, byte[] archive);

    byte[] exportDataSource(User user, String name);

    void removeDataSource(User user, String name);
    
    /**
     * Returns information about the database server which is used by the certain datasource.
     * 
     * @param name (String) - datasource id
     * @return (String) - information about database server or an error information if an exception has occurred on the version request had been invoked.
     */
    String getDBServerInfo(String name);

}
