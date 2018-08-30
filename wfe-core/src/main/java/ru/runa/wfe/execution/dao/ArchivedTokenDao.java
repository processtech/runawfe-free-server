package ru.runa.wfe.execution.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ReadOnlyGenericDao;
import ru.runa.wfe.execution.ArchivedToken;

@Component
public class ArchivedTokenDao extends ReadOnlyGenericDao<ArchivedToken> {

    public ArchivedTokenDao() {
        super(ArchivedToken.class);
    }
}
