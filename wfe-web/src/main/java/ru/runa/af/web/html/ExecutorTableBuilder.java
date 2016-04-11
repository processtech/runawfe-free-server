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

import ru.runa.af.web.form.UpdateExecutorDetailsForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;

/*
 * Created on 20.08.2004
 */
public class ExecutorTableBuilder {
    private final Executor executor;
    private final boolean enabled;
    private final PageContext pageContext;

    /**
     * Use for update
     * 
     * @param executor
     *            executor for update
     */
    public ExecutorTableBuilder(Executor executor, boolean areInputsDisabled, PageContext pageContext) {
        this.executor = executor;
        this.pageContext = pageContext;
        enabled = !areInputsDisabled;
    }

    /**
     * Use for create
     * 
     * @param isActor
     *            type of table
     */
    public ExecutorTableBuilder(boolean isActor, PageContext pageContext) {
        if (isActor) {
            executor = new Actor("", "", "", null, "", "");
        } else {
            executor = new Group("", "");
        }
        enabled = true;
        this.pageContext = pageContext;
    }

    public Table buildTable() {
        Actor actor = (Actor) (executor instanceof Actor ? executor : null);
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_EXECUTOR_NAME, pageContext),
                UpdateExecutorDetailsForm.NEW_NAME_INPUT_NAME, executor.getName(), enabled, true));
        if (actor != null) {
            table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_ACTOR_FULL_NAME, pageContext),
                    UpdateExecutorDetailsForm.FULL_NAME_INPUT_NAME, actor.getFullName(), enabled, false));
        }
        table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_EXECUTOR_DESCRIPTION, pageContext),
                UpdateExecutorDetailsForm.DESCRIPTION_INPUT_NAME, executor.getDescription() == null ? "" : executor.getDescription(), enabled, false));
        if (actor != null) {
            table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_ACTOR_CODE, pageContext),
                    UpdateExecutorDetailsForm.CODE_INPUT_NAME, actor.getCode() != null ? actor.getCode().toString() : "", enabled, false));
            table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_ACTOR_EMAIL, pageContext),
                    UpdateExecutorDetailsForm.EMAIL_INPUT_NAME, actor.getEmail() == null ? "" : actor.getEmail(), enabled, false));
            table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_ACTOR_PHONE, pageContext),
                    UpdateExecutorDetailsForm.PHONE_INPUT_NAME, actor.getPhone() == null ? "" : actor.getPhone(), enabled, false));
        } else {
            Group group = (Group) executor;
            table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_GROUP_AD, pageContext),
                    UpdateExecutorDetailsForm.EMAIL_INPUT_NAME, group.getLdapGroupName() != null ? group.getLdapGroupName() : "", enabled, false));
        }
        return table;
    }
}
