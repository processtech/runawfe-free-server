package ru.runa.wfe.commons.dao;

import com.google.common.base.Preconditions;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReadOnlyGenericDao<T> extends CommonDao {
    protected final Log log = LogFactory.getLog(getClass());
    protected final Class<T> entityClass;

    /**
     * No default constructor, no entityClass auto-detection: when subclass is generic too (like GenericDao),
     * detection looks impossible: getActualTypeArguments() returns TypeVariableImpl instead of Class.
     */
    public ReadOnlyGenericDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

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
    @SuppressWarnings("unchecked")
    public List<T> getAll() {
        return sessionFactory.getCurrentSession().createQuery("from " + entityClass.getName()).list();
    }
}
