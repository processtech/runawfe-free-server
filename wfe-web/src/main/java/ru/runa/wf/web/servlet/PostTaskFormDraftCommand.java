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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PostTaskFormDraftCommand extends JsonAjaxCommand {
    private static boolean ENABLED = WebResources.isProcessTaskFormDraftEnabled();
    public static final JSONObject EMPTY_JSON = new JSONObject();
    private static final Pattern FILE_KEY_PATTERN = Pattern.compile("(\\d+)" + FormSubmissionUtils.FILES_MAP_QUALIFIER + "([^\\[]+)\\[(\\d+)\\]");

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
        
        Map<String, Object> userInput = new HashMap<>();
        userInput.putAll(request.getParameterMap());
        userInput.putAll(availableFiles(taskId, request));

        HashMap<String, Object> variables = readVariables(user, task, userInput);
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

    private Map<String, Object> availableFiles(long taskId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Map<String, UploadedFile> uploadedFiles = FormSubmissionUtils.getUserInputFiles(request);
        Map<String, List<Integer>> fileIdx = new HashMap<>();
        for (String key : uploadedFiles.keySet()) {
            Matcher m = FILE_KEY_PATTERN.matcher(key);
            if (m.matches()) {
                long id = Long.parseLong(m.group(1));
                String name = m.group(2);
                int index = Integer.parseInt(m.group(3));

                List<Integer> nameIdx = fileIdx.getOrDefault(name, new ArrayList<>());
                nameIdx.add(index);
                fileIdx.put(name, nameIdx);


                if (id != taskId)
                    continue;

                // сами файлы
                result.put(key, uploadedFiles.get(key));
            }
        }

        // индексы
        for (Map.Entry<String, List<Integer>> entry : fileIdx.entrySet()) {
            String idxKey = taskId + FormSubmissionUtils.FILES_MAP_QUALIFIER + entry.getKey() + FormSubmissionUtils.INDEXES_SUFFIX;
            String idxs = entry.getValue().stream().map(integer -> integer.toString()).collect(Collectors.joining(","));

            result.put(idxKey, new String[]{idxs});
        }

        return result;
    }

    private Optional<WfTask> getWfTask(User user, long taskId) {
        try {
            return Optional.of(Delegates.getTaskService().getTask(user, taskId));
        } catch (TaskDoesNotExistException e) {
            log.warn(e);
            return Optional.empty();
        }
    }

    private HashMap<String, Object> readVariables(User user, WfTask task, Map<String, ?> userInput) {
        HashMap<String, Object> variables = new HashMap<>();

        Interaction interaction = Delegates.getDefinitionService().getTaskNodeInteraction(user, task.getDefinitionId(), task.getNodeId());
        Map<String, String> errors = new HashMap<>();
        for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
            Object variable = FormSubmissionUtils.extractVariable(user, task.getId(), userInput, variableDefinition, errors);
            variables.put(variableDefinition.getName(), variable);
        }

        return variables;
    }
}
