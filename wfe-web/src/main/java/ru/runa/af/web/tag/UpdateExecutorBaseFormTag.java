package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateExecutorBaseForm")
public abstract class UpdateExecutorBaseFormTag extends SecuredObjectFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected final SecuredObject getSecuredObject() {
        return getExecutor();
    }

    @Override
    protected final Permission getSubmitPermission() {
        throw new IllegalAccessError();
    }

    protected final Executor getExecutor() {
        return Delegates.getExecutorService().getExecutor(getUser(), getIdentifiableId());
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, getExecutor());
    }
}
