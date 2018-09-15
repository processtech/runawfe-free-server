package ru.runa.wfe.commons.hibernate;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

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
}
