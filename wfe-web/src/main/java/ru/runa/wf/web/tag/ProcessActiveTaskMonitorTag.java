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

import ru.runa.common.web.Messages;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.StringsHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.wf.web.html.ProcessSwimlaneAssignmentRowBuilder;
import ru.runa.wfe.execution.ProcessPermission;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Created on 29.11.2004
 *
 * @jsp.tag name = "processActiveTaskMonitor" body-content = "empty"
 */
public class ProcessActiveTaskMonitorTag extends ProcessBaseFormTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected void fillFormData(TD tdFormElement) {
        List<WfTask> activeTasks = Delegates.getTaskService().getProcessTasks(getUser(), getIdentifiableId(), false);
        HeaderBuilder headerBuilder = new StringsHeaderBuilder(new String[] { Messages.getMessage(Messages.LABEL_STATE_NAME, pageContext),
                Messages.getMessage(Messages.LABEL_SWIMLANE, pageContext), Messages.getMessage(Messages.LABEL_SWIMLANE_ASSIGNMENT, pageContext) });
        RowBuilder rowBuilder = new ProcessSwimlaneAssignmentRowBuilder(getUser(), activeTasks, pageContext);
        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder));
    }

    @Override
    protected boolean isFormButtonVisible() {
        return false;
    }

    @Override
    protected Permission getPermission() {
        return ProcessPermission.READ;
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_INSANCE_TASKS_LIST, pageContext);
    }
}
