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
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.UpdatePermissionsOnBotStationAction;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.html.PermissionTableBuilder;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;

/**
 * @author: stan79 Date: 25.05.2008 Time: 20:04:19
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botStationConfigurationTag")
public class BotStationConfigurationTag extends IdentifiableFormTag {
    private static final long serialVersionUID = -1187003724875968614L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        PermissionTableBuilder tableBuilder = new PermissionTableBuilder(getIdentifiable(), getUser(), pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
    }

    @Override
    protected Permission getPermission() {
        return Permission.READ;
    }

    @Override
    protected Identifiable getIdentifiable() {
        return BotStation.INSTANCE;
    }

    @Override
    public String getAction() {
        return UpdatePermissionsOnBotStationAction.ACTION_PATH_NAME;
    }

    @Override
    protected String getFormButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesCommon.TITLE_PERMISSION_OWNERS.message(pageContext);
    }
}
