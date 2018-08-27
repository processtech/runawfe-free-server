package ru.runa.wfe.var.dao;

import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.var.BaseVariable;

public abstract class BaseVariableDao<T extends BaseVariable> extends GenericDao<T> {

    BaseVariableDao(Class<T> entityClass) {
        super(entityClass);
    }
}
