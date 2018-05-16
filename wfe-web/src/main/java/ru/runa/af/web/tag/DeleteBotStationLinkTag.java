/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web.tag;

import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.tag.LinkTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "deleteBotStationLink")
public class DeleteBotStationLinkTag extends LinkTag {

    private static final long serialVersionUID = -8445857392805848169L;

    @Override
    protected boolean isLinkEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
    }

    @Override
    protected String getLinkText() {
        return MessagesBot.BUTTON_DELETE_BOT_STATION.message(pageContext);
    }

}
