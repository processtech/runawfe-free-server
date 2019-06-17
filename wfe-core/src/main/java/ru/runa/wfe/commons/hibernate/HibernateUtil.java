package ru.runa.wfe.commons.hibernate;

import java.util.Map;
import lombok.val;
import org.hibernate.Hibernate;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.persister.collection.AbstractCollectionPersister;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.proxy.HibernateProxy;
import ru.runa.wfe.commons.ApplicationContextFactory;

public class HibernateUtil {

    public static <T> T unproxy(T o) {
        Hibernate.initialize(o);
        return unproxyWithoutInitialize(o);
    }

    @SuppressWarnings("unchecked")
    public static <T> T unproxyWithoutInitialize(T o) {
        return o instanceof HibernateProxy
                ? (T) (((HibernateProxy) o).getHibernateLazyInitializer().getImplementation())
                : o;
    }

    /**
     * Adopted from here: https://stackoverflow.com/a/2461084/4247442
     * (link to code snipped for cache provider-independent code; but it's deprecated since Hibernate 4 as stated in other comments).
     *
     * @param onlyClasses If non-empty, will delete 2LC only for specified entity classes and their subclasses.
     */
    public static void clearSecondLevelCaches(Class<?>... onlyClasses) {
        val sf = ApplicationContextFactory.getSessionFactory();

        Map<String, ClassMetadata> classMetadata = sf.getAllClassMetadata();
        for (val cm : classMetadata.values()) {
            val aep = (AbstractEntityPersister) cm;
            if (aep.hasCache() && clearSecondLevelCaches_shouldDoForClass(aep.getEntityType().getReturnedClass(), onlyClasses)) {
                sf.getCache().evictEntityRegion(aep.getEntityType().getReturnedClass());
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, CollectionMetadata> collMetadata = sf.getAllCollectionMetadata();
        for (val cm : collMetadata.values()) {
            val acp = (AbstractCollectionPersister) cm;
            if (acp.hasCache() && clearSecondLevelCaches_shouldDoForClass(acp.getElementType().getReturnedClass(), onlyClasses)) {
                sf.getCache().evictCollectionRegion(acp.getRole());
            }
        }
    }

    private static boolean clearSecondLevelCaches_shouldDoForClass(Class<?> entityClass, Class<?>... onlyClasses) {
        if (onlyClasses.length == 0) {
            return true;
        }
        for (val oc : onlyClasses) {
            if (oc.isAssignableFrom(entityClass)) {
                return true;
            }
        }
        return false;
    }
}
