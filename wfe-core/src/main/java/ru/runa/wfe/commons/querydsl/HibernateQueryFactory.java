package ru.runa.wfe.commons.querydsl;

import javax.inject.Provider;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HibernateQueryFactory extends com.querydsl.jpa.hibernate.HibernateQueryFactory {

    // Tried to implement Provider<Session> by HibernateQueryFactory itself, but neither "super(this)"
    // nor even "this" are allowed before super() is called. So hack around.
    // TODO Try to use Spring's constructor injection to define bean of class com.querydsl.jpa.hibernate.HibernateQueryFactory
    //      and pass external SessionProvider to it -- instead of subclassing it? Or subclass is good because we can add stuff here?
    private static class SessionProvider implements Provider<Session> {
        static SessionProvider instance;
        HibernateQueryFactory owner;

        SessionProvider() {
            instance = this;
        }

        @Override
        public Session get() {
            return owner.sessionFactory.getCurrentSession();
        }

    }


    @Autowired
    private SessionFactory sessionFactory;

    public HibernateQueryFactory() {
        super(new SessionProvider());
        SessionProvider.instance.owner = this;
    }
}
