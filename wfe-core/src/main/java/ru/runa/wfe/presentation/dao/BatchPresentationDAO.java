package ru.runa.wfe.presentation.dao;

import java.util.List;

import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.presentation.BatchPresentation;

public class BatchPresentationDAO extends GenericDAO<BatchPresentation> {
    public List<BatchPresentation> getAllShared() {
        return getHibernateTemplate().find("from BatchPresentation where is_shared = ?", true);
    }
}
