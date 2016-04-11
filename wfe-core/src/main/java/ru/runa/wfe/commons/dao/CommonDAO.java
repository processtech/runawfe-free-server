package ru.runa.wfe.commons.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Common DAO implementation with useful operations.
 * 
 * @author dofs
 * @since 4.0
 */
@SuppressWarnings("unchecked")
public abstract class CommonDAO extends HibernateDaoSupport {
    protected static final Log log = LogFactory.getLog(CommonDAO.class);

    /**
     * Load entity from database by id.
     * 
     * @return entity or <code>null</code> if no entity found.
     */
    protected <T extends Object> T get(Class<T> clazz, Long id) {
        return getHibernateTemplate().get(clazz, id);
    }

    /**
     * @return first entity from list or <code>null</code>
     */
    protected <T extends Object> T getFirstOrNull(List<T> list) {
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
     */
    protected <T extends Object> T findFirstOrNull(String hql, Object... parameters) {
        List<T> list = (List<T>) getHibernateTemplate().find(hql, parameters);
        return getFirstOrNull(list);
    }

}
