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
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.RestoreDefaultSettingsAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * @author: petrmikheev Date: 26.08.2014
 * @jsp.tag name = "systemSettings" body-content = "JSP"
 */

public class SystemSettingsTag extends TitledFormTag {
    private static final long serialVersionUID = -426375016105456L;

    protected String getTitle() {
        return Messages.getMessage(Messages.MANAGE_SETTINGS, pageContext);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
    	if (!Delegates.getExecutorService().isAdministrator(getUser()))
    		throw new AuthorizationException("No permission on this page");
        Table table = new Table();
        for (String resource : EditSettingsTag.settingsList.descendingKeySet()) {
        	TR tr = new TR();
        	String title = EditSettingsTag.getDescription(pageContext, resource);
        	tr.addElement("<td><a href=edit_settings.do?resource="+resource+">" + title + "</a></td>");
        	table.addElement(tr);
        }
        tdFormElement.addElement(table);
    }
    
    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    /*@Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_USE_DAFAULT_PROPERTIES, pageContext);
    }

    @Override
    public String getAction() {
    	return RestoreDefaultSettingsAction.RESTORE_DEFAULT_SETTINGS_ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.USE_DEFAULT_PROPERTIES_PARAMETER;
    }*/
}
