package ru.runa.wf.web.servlet;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import ru.runa.common.WebResources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.web.JsonAjaxCommand;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.TaskService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.util.SerialisationUtils;
import ru.runa.wfe.var.VariableDefinition;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

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
        HashMap<String, Object> variables = readVariables(user, taskId, request.getParameterMap());

        byte[] data = SerialisationUtils.serialize(variables);
        taskService.setTaskFormDraft(user, taskId, data);

        return EMPTY_JSON;
    }


    private HashMap<String, Object> readVariables(User user, long taskId, Map<String, String[]> userInput) {
        HashMap<String, Object> variables = new HashMap<>();

        WfTask task = Delegates.getTaskService().getTask(user, taskId);
        Interaction interaction = Delegates.getDefinitionService().getTaskNodeInteraction(user, task.getDefinitionId(), task.getNodeId());
        Map<String, String> errors = new HashMap<>();
        for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
            Object variable = FormSubmissionUtils.extractVariable(user, userInput, variableDefinition, errors);
            variables.put(variableDefinition.getName(), variable);
        }

        return variables;
    }
}
