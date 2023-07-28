package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.web.PortletUrlType;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createActorLink")
public class CreateActorLinkTag extends CreateExecutorLinkTag {

    private static final long serialVersionUID = -7064081489072327132L;

    @Override
    protected String getLinkText() {
        return MessagesExecutor.CREATE_ACTOR.message(pageContext);
    }

    public static final String FORWARD = "create_actor";

    @Override
    protected String getHref() {
        return Commons.getForwardUrl(FORWARD, pageContext, PortletUrlType.Render);
    }
}
