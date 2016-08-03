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
package ru.runa.wf.web.tag;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.json.simple.JSONValue;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.CssClassStrategy;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.AssignTaskCheckboxTDBuilder;
import ru.runa.wf.web.html.TaskUrlStrategy;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;

/**
 * Tasks list form tag for special administrative purposes
 * 
 * Created on 15.07.2016
 * 
 * @author Alexander Mamchur
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listTasksAdministerForm")
public class ListTasksAdministerTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfTask> tasks = Delegates.getTaskService().getTasks(getUser(), batchPresentation);
        Table table = buildTasksTable(pageContext, batchPresentation, tasks, getReturnAction(), false);
        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, tasks.size());
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(table);
        navigation.addPagingNavigationTable(tdFormElement);

        // Build current filtered tasks ID-s string (in JSON format for common purposes)
        String batchName = batchPresentation.getName();
        if (!batchName.equals("label.batch_presentation_default_name") && tasks.size() > 0) {
            List<Long> ids = new ArrayList<Long>(tasks.size());
            for (WfTask tsk : tasks) {
                ids.add(tsk.getId());
            }
            String tasksIds = JSONValue.toJSONString(ids);
            pageContext.setAttribute("tasksIds", tasksIds, PageContext.REQUEST_SCOPE);
        }
    }

    public static Table buildTasksTable(PageContext pageContext, BatchPresentation batchPresentation, List<WfTask> tasks, String returnAction,
            boolean disableCheckbox) {

        TDBuilder[] builders = BatchPresentationUtils.getBuilders(new TDBuilder[] { new AssignTaskCheckboxTDBuilder(!disableCheckbox) },
                batchPresentation, new TDBuilder[] {});

        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 1, 0, returnAction, pageContext);
        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(tasks, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER, null, new TaskUrlStrategy(pageContext), builders);
        rowBuilder.setCssClassStrategy(new TasksCssClassStrategy());
        return new TableBuilder().build(headerBuilder, rowBuilder);
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_TASKS.message(pageContext);
    }

    public static class TasksCssClassStrategy implements CssClassStrategy {
        private final Date currentDate = new Date();

        @Override
        public String getClassName(Object item, User user) {
            WfTask task = (WfTask) item;
            Date deadlineDate = task.getDeadlineDate();
            if (deadlineDate == null) {
                return null;
            }
            if (task.isEscalated()) {
                return "escalatedTask";
            }
            if (task.isAcquiredBySubstitution()) {
                return "substitutionTask";
            }
            if (deadlineDate.before(currentDate)) {
                return "deadlineExpired";
            }
            if (task.getDeadlineWarningDate() != null && task.getDeadlineWarningDate().before(currentDate)) {
                return "deadlineAlmostExpired";
            }
            return "deadlineExists";
        }

        @Override
        public String getCssStyle(Object item) {
            if (((WfTask) item).isFirstOpen()) {
                return "font-weight: bold;";
            }
            return null;
        }
    }

}
