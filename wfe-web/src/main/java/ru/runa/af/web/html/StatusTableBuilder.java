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

import org.apache.ecs.html.Table;

import ru.runa.af.web.form.UpdateStatusForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.wfe.user.Actor;

public class StatusTableBuilder {
    private final boolean enabled;
    private final PageContext pageContext;
    private final Actor actor;

    public StatusTableBuilder(Actor actor, boolean disabled, PageContext pageContext) {
        this.actor = actor;
        enabled = !disabled;
        this.pageContext = pageContext;
    }

    public Table build() {
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        table.addElement(HTMLUtils.createCheckboxRow(Messages.getMessage(Messages.LABEL_ACTOR_IS_ACTIVE, pageContext),
                UpdateStatusForm.IS_ACTIVE_INPUT_NAME, actor.isActive(), enabled, false));
        return table;
    }
}
