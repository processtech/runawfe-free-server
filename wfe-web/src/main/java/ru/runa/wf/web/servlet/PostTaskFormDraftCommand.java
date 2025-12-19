package ru.runa.wf.web.servlet;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.common.WebResources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.service.client.DelegateTaskVariableProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.util.SerialisationUtils;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PostTaskFormDraftCommand extends JsonAjaxCommand {
    private static boolean ENABLED = WebResources.isProcessTaskFormDraftEnabled();
    public static final JSONObject EMPTY_JSON = new JSONObject();

    @Override
    protected JSONAware execute(User user, HttpServletRequest request) throws Exception {
        if (!ENABLED)
            return EMPTY_JSON;

        if (!"POST".equals(request.getMethod())) {
            throw new IllegalArgumentException();
        }

        TaskService taskService = Delegates.getTaskService();
        long taskId = Long.parseLong(request.getParameter("taskId"));

        Optional<WfTask> wfTaskOpt = getWfTask(user, taskId);
        if (!wfTaskOpt.isPresent())
            return EMPTY_JSON;

        WfTask task = wfTaskOpt.get();
        HashMap<String, Object> variables = readVariables(user, task, request.getParameterMap());
        VariableProvider variableProvider = new DelegateTaskVariableProvider(user, task);
        HashMap<String, Object> filtered = new HashMap<>();

        // Сохраняем только те что отличаются
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            WfVariable existing = variableProvider.getVariable(entry.getKey());
            if (null != existing
                    && Objects.equals(entry.getValue(), existing.getValue())) {
                continue;
            }

            filtered.put(entry.getKey(), entry.getValue());
        }

        if (!filtered.isEmpty()) {
            byte[] data = SerialisationUtils.serialize(filtered);
            taskService.setTaskFormDraft(user, taskId, data);
        }

        return EMPTY_JSON;
    }

    private Optional<WfTask> getWfTask(User user, long taskId) {
        try {
            return Optional.of(Delegates.getTaskService().getTask(user, taskId));
        } catch (TaskDoesNotExistException e) {
            log.warn(e);
            return Optional.empty();
        }
    }

    private HashMap<String, Object> readVariables(User user, WfTask task, Map<String, String[]> userInput) {
        HashMap<String, Object> variables = new HashMap<>();

        Interaction interaction = Delegates.getDefinitionService().getTaskNodeInteraction(user, task.getDefinitionId(), task.getNodeId());
        Map<String, String> errors = new HashMap<>();
        for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
            Object variable = FormSubmissionUtils.extractVariable(user, userInput, variableDefinition, errors);
            variables.put(variableDefinition.getName(), variable);
        }

        return variables;
    }
}
