package ru.runa.common.web.action;

import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.user.User;

public abstract class SecuredObjectAction extends ActionBase {

    protected abstract SecuredObject getSecuredObject(User user, Long identifiableId);
}
