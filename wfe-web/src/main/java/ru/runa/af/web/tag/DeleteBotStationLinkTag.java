package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deleteBotStationLink")
public class DeleteBotStationLinkTag extends LinkTag {

    private static final long serialVersionUID = -8445857392805848169L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredSingleton.BOTSTATIONS);
    }

    @Override
    protected String getLinkText() {
        return MessagesBot.BUTTON_DELETE_BOT_STATION.message(pageContext);
    }

}
