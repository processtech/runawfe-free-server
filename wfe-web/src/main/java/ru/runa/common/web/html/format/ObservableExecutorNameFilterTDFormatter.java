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
package ru.runa.common.web.html.format;

import java.util.Set;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.task.logic.ITaskListBuilder;
import ru.runa.wfe.task.logic.TaskListBuilder;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

public class ObservableExecutorNameFilterTDFormatter extends FilterTDFormatter {

    @Override
    public void formatTd(TD td, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        String[] stringConditions = filterCriteria.getFilterTemplates();
        Input filterInput = new Input(Input.TEXT, TableViewSetupForm.FILTER_CRITERIA, stringConditions[0]);
        td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        td.addElement(filterInput);
        td.addElement(Entities.NBSP);
        Actor actor = ProfileHttpSessionHelper.getProfile(pageContext.getSession()).getActor();
        try {
            Utils.getTransactionManager().begin();
            Set<Executor> executors = ((TaskListBuilder) ApplicationContextFactory.getContext().getBean(ITaskListBuilder.class)).getObservableExecutors(actor, "");
            Utils.getTransactionManager().rollback();
            String title = MessagesProcesses.TITLE_OBSERVABLE_EXECUTORS.message(pageContext) + " (" + executors.size() + "):<br/>";
            int maxCount = 20;
            for (Executor executor : executors) {
                title += executor.getName() + "<br/>";
                if (--maxCount <= 0) {
                    title += "...<br/>";
                    break;
                }
            }
            filterInput.setTitle(title);
        } catch (Exception e) {
            throw new InternalApplicationException(e);
        }
    }

}
