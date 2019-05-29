package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "synchronizeExecutorsLink")
public class SynchronizeExecutorsLinkTag extends LinkTag {

    private static final long serialVersionUID = -7064081489072327132L;

    @Override
    protected String getLinkText() {
        return MessagesOther.LABEL_SYNCHRONIZE_LDAP.message(pageContext);
    }

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getExecutorService().isAdministrator(getUser());
    }

    @Override
    protected String getHref() {
        return Commons.getActionUrl("synchronizeExecutors", pageContext, PortletUrlType.Action);
    }
}
