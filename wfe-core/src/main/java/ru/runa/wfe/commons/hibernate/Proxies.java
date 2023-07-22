package ru.runa.wfe.commons.hibernate;

import org.hibernate.proxy.HibernateProxy;

public class Proxies {

    public static <T extends Object> T getImplementation(T object) {
        if (object instanceof HibernateProxy) {
            object = (T) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        return object;
    }

}
