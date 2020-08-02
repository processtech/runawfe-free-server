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

import java.util.List;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.html.ProcessSwimlaneAssignmentRowBuilder;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "processActiveTaskMonitor")
public class ProcessActiveTaskMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<WfTask> activeTasks = Delegates.getTaskService().getProcessTasks(getUser(), getIdentifiableId(), false);
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(new String[] { MessagesProcesses.LABEL_STATE_NAME.message(pageContext),
                MessagesProcesses.LABEL_SWIMLANE.message(pageContext), MessagesProcesses.LABEL_SWIMLANE_ASSIGNMENT.message(pageContext),
                MessagesProcesses.LABEL_CREATE_TIME.message(pageContext), MessagesProcesses.LABEL_END_TIME.message(pageContext),
                MessagesProcesses.LABEL_CURRENT_DURATION.message(pageContext), MessagesProcesses.LABEL_REMAINING_TIME.message(pageContext),
                MessagesProcesses.LABEL_ASSIGNMENT_TIME.message(pageContext) });
        RowBuilder rowBuilder = new ProcessSwimlaneAssignmentRowBuilder(getUser(), activeTasks, pageContext);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.READ;
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_INSANCE_TASKS_LIST.message(pageContext);
    }
}
