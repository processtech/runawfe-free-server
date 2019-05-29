package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author stan79
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deleteBotLink")
public class DeleteBotLinkTag extends LinkTag {

    private static final long serialVersionUID = -8445857392805848169L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.BOTSTATIONS);
    }

    @Override
    protected String getLinkText() {
        return MessagesBot.BUTTON_DELETE_BOT.message(pageContext);
    }
}
