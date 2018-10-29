package ru.runa.wf.web.tag;

import java.util.List;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.collect.Lists;

/**
 * Created on 14.04.2008
 * 
 * @author YSK
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "taskDetails")
public class TaskDetailsTag extends BatchReturningTitledFormTag {
    private static final long serialVersionUID = -8864271538433581304L;

    private Long taskId;
    private Long actorId;
    private boolean buttonEnabled = false;

    private Long getTaskId() {
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
    protected void fillFormElement(TD tdFormElement) {
        BatchPresentation batchPresentation = getProfile().getActiveBatchPresentation(BatchPresentationConsts.ID_TASKS).clone();
        batchPresentation.setFieldsToGroup(new int[0]);
        WfTask task = Delegates.getTaskService().getTask(getUser(), getTaskId());
        List<String> variableNamesToDisplay = batchPresentation.getDynamicFieldsToDisplay(false);
        for (String variableName : variableNamesToDisplay) {
            WfVariable variable = Delegates.getExecutionService().getVariable(getUser(), task.getProcessId(), variableName);
            if (variable != null) {
                task.addVariable(variable);
            }
        }
        this.buttonEnabled = task.isGroupAssigned() && !task.isReadOnly();
        String url = getReturnAction() + "?" + IdForm.ID_INPUT_NAME + "=" + taskId + "&" + ProcessForm.ACTOR_ID_INPUT_NAME + "=" + actorId;
        tdFormElement.addElement(ListTasksFormTag.buildTasksTable(pageContext, batchPresentation, Lists.newArrayList(task), url, true));
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, String.valueOf(taskId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, ProcessForm.ACTOR_ID_INPUT_NAME, String.valueOf(actorId)));
        tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.HIDDEN_ONE_TASK_INDICATOR, WebResources.HIDDEN_ONE_TASK_INDICATOR));
        if (task.getOwner() != null) {
            tdFormElement.addElement(new Input(Input.HIDDEN, WebResources.HIDDEN_TASK_PREVIOUS_OWNER_ID, task.getOwner().getId().toString()));
        }
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return buttonEnabled;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_ACCEPT_TASK.message(pageContext);
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.ACCEPT_TASK_PARAMETER;
    }
}
