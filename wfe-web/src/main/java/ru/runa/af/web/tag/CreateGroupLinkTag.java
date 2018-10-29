package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Commons;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * Created on 03.09.2004
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createGroupLink")
public class CreateGroupLinkTag extends CreateExecutorLinkTag {

    private static final long serialVersionUID = -4287998105517572084L;

    @Override
    protected String getLinkText() {
        return MessagesExecutor.CREATE_GROUP.message(pageContext);
    }

    public static final String FORWARD = "create_group";

    @Override
    protected String getHref() {
        return Commons.getForwardUrl(FORWARD, pageContext, PortletUrlType.Render);
    }
}
