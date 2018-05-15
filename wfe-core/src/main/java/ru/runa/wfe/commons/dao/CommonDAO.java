package ru.runa.wfe.commons.dao;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import ru.runa.wfe.commons.querydsl.HibernateQueryFactory;

/**
 * Common DAO implementation with useful operations.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public abstract class CommonDAO extends HibernateDaoSupport {
    protected static final Log log = LogFactory.getLog(CommonDAO.class);

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    protected HibernateQueryFactory queryFactory;

    /**
     * Load entity from database by id.
     * 
     * @return entity or <code>null</code> if no entity found.
     */
    protected <T> T get(Class<T> clazz, Long id) {
        return (T)sessionFactory.getCurrentSession().get(clazz, id);
    }

    /**
     * @return first entity from list or <code>null</code>
     */
    protected <T> T getFirstOrNull(List<T> list) {
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Finds entities.
     * 
     * @param hql
     *            Hibernate query
     * @param parameters
     *            query parameters
     * @return first entity from list or <code>null</code>
     * @deprecated Use HQL's setMaxResults(1) and uniqueResult(), or QueryDSL's fetchFirst() -- instead of querying whole list from SQL server.
     */
    @Deprecated
    protected <T> T findFirstOrNull(String hql, Object... parameters) {
        Query q = sessionFactory.getCurrentSession().createQuery(hql);
        for (int i = 0; i < parameters.length; i++) {
            q.setParameter(i, parameters[i]);
        }
        return (T)q.setMaxResults(1).uniqueResult();
    }

}
