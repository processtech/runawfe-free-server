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

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wf.web.TaskFormBuilderFactory;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "taskForm")
public class TaskFormTag extends WFFormTag {
    private static final long serialVersionUID = -8864271538433581304L;

    private Long taskId;
    private Long actorId;

    public Long getTaskId() {
        return taskId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getActorId() {
        return actorId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    @Override
    protected Long getDefinitionVersionId() {
        return Delegates.getTaskService().getTask(getUser(), taskId).getDefinitionId();
    }

    @Override
    protected Interaction getInteraction() {
        WfTask task = Delegates.getTaskService().getTask(getUser(), taskId);
        return Delegates.getDefinitionService().getTaskNodeInteraction(getUser(), task.getDefinitionId(), task.getNodeId());
    }

    @Override
    protected String buildForm(Interaction interaction) {
        TaskFormBuilder taskFormBuilder = TaskFormBuilderFactory.createTaskFormBuilder(getUser(), pageContext, interaction);
        WfTask task = Delegates.getTaskService().getTask(getUser(), taskId);
        return taskFormBuilder.build(task);
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        super.fillFormElement(tdFormElement);
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, ProcessForm.ACTOR_ID_INPUT_NAME, String.valueOf(actorId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.ACTION_MAPPING_SUBMIT_TASK_DISPATCHER, "redirectEnabled"));
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.EXECUTE_TASK_PARAMETER;
    }

    @Override
    protected String getTitle() {
        if (!isSubmitButtonEnabled()) {
            return MessagesProcesses.TITLE_TASK_FORM.message(pageContext) + " " + MessagesProcesses.TITLE_VIEW_ONLY.message(pageContext);
        }
        return super.getTitle();
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return !Delegates.getTaskService().getTask(getUser(), getTaskId()).isReadOnly();
    }

}
