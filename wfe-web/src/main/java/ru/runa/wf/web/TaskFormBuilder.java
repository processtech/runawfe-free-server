package ru.runa.wf.web;

import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import ru.runa.common.WebResources;
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
import ru.runa.wfe.var.dto.WfVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * @param task
     * @return
     */
    protected Map<String, Object> loadDraftData(User user, WfTask task) {
        if (!WebResources.isProcessTaskFormDraftEnabled())
            return Collections.emptyMap();

        if (null == task)
            return Collections.emptyMap();

        TaskService taskService = Delegates.getTaskService();
        WfTaskFormDraft draft = taskService.getTaskFormDraft(user, task.getId());
        if (null == draft)
            return Collections.emptyMap();

        try {
            return (Map<String, Object>) SerialisationUtils.deserialize(draft.getData());
        } catch (Exception e) {
            log.warn(e);
            return Collections.emptyMap();
        }
    }

    @SneakyThrows
    protected String modifyFtlSelectOptions(String ftlFormData, VariableProvider variableProvider) {
        Pattern p = Pattern.compile(
                "<SELECT\\b[^>]*>.*?</SELECT>",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );

        StringBuffer result = new StringBuffer();
        Matcher m = p.matcher(ftlFormData);

        while (m.find()) {
            String selectBlock = m.group();

            Document document = Jsoup.parseBodyFragment(selectBlock);
            Element select = document.body().child(0);
            String name = select.attr("name");
            WfVariable variable = variableProvider.getVariable(name);

            if (null != variable && null != variable.getValue()) {
                Object value = variable.getValue();
                for (Node optionNode : select.selectNodes("option")) {
                    Element option = (Element) optionNode;

                    option.removeAttr("selected");
                    if (checkSelected(option.attr("value"), value)) {
                        option.attr("selected", "selected");
                    }
                }

                selectBlock = document.body().html();
            }

            m.appendReplacement(result, Matcher.quoteReplacement(selectBlock));
        }

        m.appendTail(result);

        return result.toString();
    }

    private boolean checkSelected(String viewValue, Object dbValue) {
        if (dbValue instanceof Collection) {
            for (Object dbVal : (Collection) dbValue) {
                if (null != dbVal && StringUtils.equals(viewValue, dbVal.toString()))
                    return true;
            }
        } else {
            if (null != dbValue && StringUtils.equals(viewValue, dbValue.toString()))
                return true;
        }

        return false;
    }
}
