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

import ru.runa.af.web.action.RestoreDefaultSettingsAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.tag.TitledFormTag;

/**
 * @author: petrmikheev Date: 16.09.2014
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "dropSettings")
public class DropSettingsTag extends TitledFormTag {
    private static final long serialVersionUID = -426375016105456L;

    @Override
    protected String getTitle() {
        return MessagesCommon.MAIN_MENU_ITEM_SETTINGS.message(pageContext);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesOther.BUTTON_USE_DAFAULT_PROPERTIES.message(pageContext);
    }

    @Override
    public String getAction() {
        return RestoreDefaultSettingsAction.RESTORE_DEFAULT_SETTINGS_ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.USE_DEFAULT_PROPERTIES_PARAMETER;
    }

    @Override
    protected boolean isCancelButtonEnabled() {
        return true;
    }

    @Override
    protected String getCancelButtonAction() {
        return "manage_settings.do";
    }
}
