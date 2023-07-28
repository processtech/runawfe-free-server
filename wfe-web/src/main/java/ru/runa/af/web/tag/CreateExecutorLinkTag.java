package ru.runa.af.web.tag;

import ru.runa.common.web.tag.LinkTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 03.09.2004
 */
public abstract class CreateExecutorLinkTag extends LinkTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.CREATE_EXECUTOR, SecuredSingleton.SYSTEM);
    }
}
