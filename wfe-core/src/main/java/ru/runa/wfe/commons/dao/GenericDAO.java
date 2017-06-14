package ru.runa.wfe.commons.dao;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.google.common.base.Preconditions;

/**
 * General DAO implementation (type-safe generic DAO pattern).
 * 
 * @author dofs
 * @since 4.0
 * 
 * @param <T>
 *            entity class
 */
@SuppressWarnings("unchecked")
public abstract class GenericDAO<T extends Object> extends CommonDAO implements IGenericDAO<T> {
    protected static final Log log = LogFactory.getLog(GenericDAO.class);
    private final Class<T> entityClass;

    /**
     * Constructor
     */
    public GenericDAO() {
        Type t = getClass().getGenericSuperclass();
        ParameterizedType pt = (ParameterizedType) t;
        entityClass = (Class<T>) pt.getActualTypeArguments()[0];
    }

    @Override
    public T get(Long id) {
        Preconditions.checkArgument(id != null);
        return get(entityClass, id);
    }

    /**
     * Load entity from database by id.
     * 
     * @return entity.
     */
    public T getNotNull(Long id) {
        T entity = get(id);
        checkNotNull(entity, id);
        return entity;
    }

    /**
     * Checks that entity is not null. Throws exception in that case. Used in *NotNull methods. Expected to be overriden in subclasses.
     * 
     * @param entity
     *            test entity
     * @param identity
     *            search data
     * @throws Exception
     *             or any of its subclass (NullPointerException in base implementation)
     */
    protected void checkNotNull(T entity, Object identity) {
        if (entity == null) {
            throw new NullPointerException("No entity found");
        }
    }

    /**
     * Load all entities from database.
     * 
     * @return entities list, not <code>null</code>.
     */
    public List<T> getAll() {
        return getHibernateTemplate().loadAll(entityClass);
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
    @Override
    protected T findFirstOrNull(final String hql, final Object... parameters) {
        List<T> list = getHibernateTemplate().executeFind(new HibernateCallback<List<T>>() {

            @Override
            public List<T> doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setMaxResults(1);
                if (parameters != null) {
                    for (int i = 0; i < parameters.length; i++) {
                        query.setParameter(i, parameters[i]);
                    }
                }
                return query.list();
            }
        });
        return getFirstOrNull(list);
    }

    /**
     * Saves transient entity.
     * 
     * @return saved entity.
     */
    public T create(T entity) {
        getHibernateTemplate().save(entity);
        return entity;
    }

    /**
     * Updates entity.
     * 
     * @param entity
     *            detached entity
     */
    public T update(T entity) {
        return getHibernateTemplate().merge(entity);
    }

    /**
     * Flush all pending saves, updates and deletes to the database.
     */
    public void flushPendingChanges() {
        // TODO flush?
        getHibernateTemplate().flush();
    }

    /**
     * Deletes entity from DB by id.
     */
    public void delete(Long id) {
        Preconditions.checkNotNull(id);
        delete(getNotNull(id));
    }

    /**
     * Deletes entities from DB by ids.
     */
    public void delete(List<Long> ids) {
        Preconditions.checkNotNull(ids);
        for (Long id : ids) {
            delete(id);
        }
    }

    /**
     * Deletes entity from DB
     */
    public void delete(T entity) {
        Preconditions.checkNotNull(entity);
        getHibernateTemplate().delete(entity);
    }

}
