package ru.runa.wf.web;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import lombok.extern.apachecommons.CommonsLog;
import ru.runa.common.web.MessagesException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.service.client.DelegateDefinitionVariableProvider;
import ru.runa.wfe.service.client.DelegateTaskVariableProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFormDraft;
import ru.runa.wfe.user.User;
import ru.runa.wfe.util.SerialisationUtils;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.VariableProvider;

/**
 * Created on 17.11.2004
 */
@CommonsLog
public abstract class TaskFormBuilder {
    protected User user;
    protected PageContext pageContext;
    protected Interaction interaction;
    protected Long definitionId;
    protected WfTask task;

    public void setUser(User user) {
        this.user = user;
    }

    public void setInteraction(Interaction interaction) {
        this.interaction = interaction;
    }

    public void setPageContext(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public final String build(Long definitionId) {
        this.definitionId = definitionId;
        if (interaction.hasForm()) {
            VariableProvider variableProvider = new DelegateDefinitionVariableProvider(user, definitionId);
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            Map<String, Object> map = FormSubmissionUtils.getPreviousUserInputVariables(request);
            if (map != null) {
                variableProvider = new MapDelegableVariableProvider(map, variableProvider);
            }
            return buildForm(variableProvider, definitionId);
        } else {
            return buildEmptyForm();
        }
    }

    public final String build(WfTask task) {
        this.definitionId = task.getDefinitionId();
        this.task = task;
        if (interaction.hasForm()) {
            VariableProvider variableProvider = new DelegateTaskVariableProvider(user, task);
            HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            Map<String, Object> map = FormSubmissionUtils.getPreviousUserInputVariables(request);
            if (map != null) {
                variableProvider = new MapDelegableVariableProvider(map, variableProvider);
            }
            return buildForm(variableProvider, task.getDefinitionId());
        } else {
            return buildEmptyForm();
        }
    }

    private String buildForm(VariableProvider variableProvider, Long definitionId) {
        String form = buildForm(variableProvider);
        return FormPresentationUtils.adjustForm(pageContext, definitionId, form, interaction.getRequiredVariableNames());
    }

    protected abstract String buildForm(VariableProvider variableProvider);

    private String buildEmptyForm() {
        String message = "Task form is not defined";
        if (pageContext != null) {
            message = MessagesException.ERROR_TASK_FORM_NOT_DEFINED.message(pageContext);
        }
        return message;
    }

    /**
     * Извлекаем данные, что сохранили тут {@link ru.runa.wf.web.servlet.PostTaskFormDraftCommand#execute(ru.runa.wfe.user.User, javax.servlet.http.HttpServletRequest)}
     *
     * @param user
     * @param taskId
     * @return
     */
    protected Map<String, Object> loadDraftData(User user, Long taskId) {
        TaskService taskService = Delegates.getTaskService();
        WfTaskFormDraft draft = taskService.getTaskFormDraft(user, taskId);
        if (null == draft)
            return Collections.emptyMap();

        try {
            return (Map<String, Object>) SerialisationUtils.deserialize(draft.getData());
        } catch (Exception e) {
            log.warn(e);
            return Collections.emptyMap();
        } 
    }
}
