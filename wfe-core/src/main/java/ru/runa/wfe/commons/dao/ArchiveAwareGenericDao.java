package ru.runa.wfe.commons.dao;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;

/**
 * Base class for synthetic DAOs which delegate to two normal DAOs (like ProcessDao2 which delegates to two ProcessDao and ArchivedProcessDao).
 * <p>
 * Introduced to support process archiving.
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ArchiveAwareGenericDao<T, CT extends T, CD extends GenericDao<CT>, AT extends T, AD extends ReadOnlyGenericDao<AT>> {
    protected final CD currentDao;
    protected final AD archivedDao;

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected HibernateQueryFactory queryFactory;

    public T get(Long id) {
        T entity = currentDao.get(id);
        return entity != null ? entity : archivedDao.get(id);
    }

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
}
