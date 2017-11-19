package ru.runa.wfe.redmine.api;

import ru.runa.wfe.redmine.api.impl.RedmineFacadeImpl;

public class RedmineFacadeFactory {
    public static RedmineFacade create() {
        return new RedmineFacadeImpl("context.xml");
    }
}
