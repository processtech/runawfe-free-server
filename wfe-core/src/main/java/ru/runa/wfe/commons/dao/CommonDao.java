package ru.runa.wfe.commons.dao;

import java.util.Map;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.support.DaoSupport;
import org.springframework.util.Assert;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;

/**
 * Common DAO implementation with useful operations.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public abstract class CommonDao extends DaoSupport {

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected HibernateQueryFactory queryFactory;

    @Override
    protected final void checkDaoConfig() throws IllegalArgumentException {
        Assert.notNull(sessionFactory);
        Assert.notNull(queryFactory);
    }

    /**
     * Load entity from database by id.
     * 
     * @return entity or <code>null</code> if no entity found.
     */
    protected <T> T get(Class<T> clazz, Long id) {
        return sessionFactory.getCurrentSession().get(clazz, id);
    }

    /**
     * Finds entities.
     *
     * @param hql
     *            Hibernate query
     * @param parameters
     *            query parameters
     * @return first entity from list or <code>null</code>
     */
    protected <T> T findFirstOrNull(String hql, Map<String, Object> parameters) {
        Query q = sessionFactory.getCurrentSession().createQuery(hql);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        return (T) q.setMaxResults(1).uniqueResult();
    }
}
