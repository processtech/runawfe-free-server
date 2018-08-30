package ru.runa.wfe.execution.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedProcess;

@Component
public class ArchivedProcessDao extends ReadOnlyGenericDao<ArchivedProcess> {

    public ArchivedProcessDao() {
        super(ArchivedProcess.class);
    }
}
