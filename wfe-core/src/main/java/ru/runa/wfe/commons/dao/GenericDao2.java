package ru.runa.wfe.commons.dao;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Base class for synthetic DAOs which delegate to two normal DAOs (like ProcessDao2 which delegates to two ProcessDao and ArchivedProcessDao).
 * <p>
 * Introduced to support process archiving.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class GenericDao2<T, T1 extends T, D1 extends GenericDao<T1>, T2 extends T, D2 extends GenericDao<T2>> implements GenericDaoApi<T> {
    protected final D1 dao1;
    protected final D2 dao2;

    @Override
    public T get(Long id) {
        T entity = dao1.get(id);
        return entity != null ? entity : dao2.get(id);
    }

    @Override
    public T getNotNull(Long id) {
        T entity = get(id);
        checkNotNull(entity, id);
        return entity;
    }

    protected void checkNotNull(T entity, Long id) {
        if (entity == null) {
            throw new NullPointerException("No entity found");
        }
    }

    public T1 create(T1 entity) {
        return dao1.create(entity);
    }
}
