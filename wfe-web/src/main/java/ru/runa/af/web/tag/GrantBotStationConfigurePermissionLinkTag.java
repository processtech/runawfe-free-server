package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * User: stan79 Date: 27.05.2008 Time: 12:38:49
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "grantBotStationConfigurePermissionLink")
public class GrantBotStationConfigurePermissionLinkTag extends LinkTag {

    private static final long serialVersionUID = 8676461606886894804L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
    }

    @Override
    protected String getLinkText() {
        return MessagesBot.BUTTON_BOT_STATION_CONFIGURE_PERMISSION.message(pageContext);
    }

}
