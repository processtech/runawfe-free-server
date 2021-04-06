package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Commons;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createRelationLink")
public class CreateRelationLinkTag extends LinkTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getHref() {
        return Commons.getActionUrl("create_relation.do", pageContext, PortletUrlType.Action);
    }

    @Override
    protected String getLinkText() {
        return MessagesExecutor.LINK_CREATE_RELATION.message(pageContext);
    }

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredSingleton.RELATIONS);
    }
}
