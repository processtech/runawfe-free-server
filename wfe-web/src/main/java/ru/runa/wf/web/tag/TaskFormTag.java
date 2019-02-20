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
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "taskForm")
public class TaskFormTag extends WFFormTag {
    private static final long serialVersionUID = -8864271538433581304L;

    private Long taskId;

    public Long getTaskId() {
        return taskId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
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
