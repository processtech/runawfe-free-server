package ru.runa.af.web.tag;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.LinkTag;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * User: stan79 Date: 27.05.2008 Time: 12:38:49
 * 
 * @jsp.tag name = "grantBotStationConfigurePermissionLink" body-content =
 *          "empty"
 */
public class GrantBotStationConfigurePermissionLinkTag extends LinkTag {

    private static final long serialVersionUID = 8676461606886894804L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), BotStationPermission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
    }

    @Override
    protected String getLinkText() {
        return Messages.getMessage(Messages.BUTTON_BOT_STATION_CONFIGURE_PERMISSION, pageContext);
    }

}
