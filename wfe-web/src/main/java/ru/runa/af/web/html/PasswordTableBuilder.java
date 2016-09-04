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
package ru.runa.af.web.html;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.Table;

import ru.runa.af.web.form.UpdatePasswordForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;

public class PasswordTableBuilder {
    private final boolean enabled;
    private final PageContext pageContext;

    public PasswordTableBuilder(boolean disabled, PageContext pageContext) {
        enabled = !disabled;
        this.pageContext = pageContext;
    }

    public Table build() {
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        Input passwordInput = HTMLUtils.createInput(Input.PASSWORD, UpdatePasswordForm.PASSWORD_INPUT_NAME, "", enabled, true);
        table.addElement(HTMLUtils.createRow(MessagesCommon.PASSWORD.message(pageContext), passwordInput));
        Input passwordConfirmInput = HTMLUtils.createInput(Input.PASSWORD, UpdatePasswordForm.PASSWORD_CONFIRM_INPUT_NAME, "", enabled, true);
        table.addElement(HTMLUtils.createRow(MessagesCommon.PASSWORD_CONFIRM.message(pageContext), passwordConfirmInput));
        return table;
    }
}
