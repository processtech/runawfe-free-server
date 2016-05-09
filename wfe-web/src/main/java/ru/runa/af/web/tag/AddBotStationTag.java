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

import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.CreateBotStationAction;
import ru.runa.af.web.html.BotStationTableBuilder;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;

/**
 * User: stanley Date: 08.06.2008 Time: 13:32:44
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "addBotStationTag")
public class AddBotStationTag extends TitledFormTag {
    private static final long serialVersionUID = 1920713038009470026L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Table table = BotStationTableBuilder.createBotStationDetailsTable(pageContext, "", "");
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_ADD_BOT_STATION.message(pageContext);
    }

    @Override
    public String getButtonAlignment() {
        return "right";
    }

    @Override
    protected String getFormButtonName() {
        return MessagesBot.BUTTON_ADD_BOT_STATION.message(pageContext);
    }

    @Override
    public String getAction() {
        return CreateBotStationAction.CREATE_BOT_STATION_ACTION_PATH;
    }
}
