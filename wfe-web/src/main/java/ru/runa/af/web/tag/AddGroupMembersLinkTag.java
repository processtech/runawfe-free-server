package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.IdLinkBaseTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "addGroupMembersLink")
public class AddGroupMembersLinkTag extends IdLinkBaseTag {

    private static final long serialVersionUID = 2135918275689343980L;

    @Override
    protected boolean isLinkEnabled() {
        Executor executor = Delegates.getExecutorService().getExecutor(getUser(), getIdentifiableId());
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, executor);
    }

    @Override
    protected String getLinkText() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

}
