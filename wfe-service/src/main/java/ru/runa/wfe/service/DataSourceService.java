package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.user.User;

public interface DataSourceService {

    List<String> getNames();

    void importDataSource(User user, byte[] archive);

    byte[] exportDataSource(User user, String name);

    void removeDataSource(User user, String name);
}
