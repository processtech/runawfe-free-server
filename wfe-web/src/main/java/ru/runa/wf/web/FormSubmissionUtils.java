package ru.runa.wf.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;

import ru.runa.common.web.Commons;
import ru.runa.common.web.RequestWebHelper;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.commons.ftl.FormComponentExtractionModel;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionPostProcessor;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateExecutorLoader;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FormSubmissionUtils {
    public static final Object IGNORED_VALUE = new Object();
    public static final String INDEXES_SUFFIX = ".indexes";
    private static final Log log = LogFactory.getLog(FormSubmissionUtils.class);
    private static final String USER_DEFINED_VARIABLES = "UserInputVariables";
    private static final String USER_INPUT_ERRORS = "UserInputErrors";
    private static final String USER_INPUT_FILES = "UserInputFiles";
    private static final String FILES_MAP_QUALIFIER = ":";

    public static void saveUserInputErrors(HttpServletRequest request, Map<String, String> errors) {
        request.setAttribute(USER_INPUT_ERRORS, errors);
    }

    public static Map<String, String> getUserInputErrors(ServletRequest request) {
        Map<String, String> map = (Map<String, String>) request.getAttribute(USER_INPUT_ERRORS);
        if (map == null) {
            map = Maps.newHashMap();
        }
        return map;
    }

    public static Map<String, UploadedFile> getUserInputFiles(HttpServletRequest request, String taskId) {
        Preconditions.checkNotNull(taskId, "taskId");
        Map<String, UploadedFile> inputsMap = Maps.newHashMap();
        for (Entry<String, UploadedFile> entry : getUserInputFiles(request).entrySet()) {
            String[] splitted = entry.getKey().split(FILES_MAP_QUALIFIER);
            if (splitted[0].equals(taskId)) {
                inputsMap.put(splitted[1], entry.getValue());
            }
        }
        return inputsMap;
    }

    public static void addUserInputFile(HttpServletRequest request, String taskId, String variableName, UploadedFile file) {
        Preconditions.checkNotNull(taskId, "taskId");
        String key = taskId + FormSubmissionUtils.FILES_MAP_QUALIFIER + variableName;
        getUserInputFiles(request).put(key, file);
    }

    public static void removeUserInputFile(HttpServletRequest request, String taskId, String variableName) {
        Preconditions.checkNotNull(taskId, "taskId");
        String key = taskId + FormSubmissionUtils.FILES_MAP_QUALIFIER + variableName;
        getUserInputFiles(request).remove(key);
    }

    public static void clearUserInputFiles(HttpServletRequest request) {
        getUserInputFiles(request).clear();
    }

    public static Map<String, Object> getPreviousUserInputVariables(HttpServletRequest request, Interaction interaction,
            IVariableProvider variableProvider) {
        return (Map<String, Object>) request.getAttribute(USER_DEFINED_VARIABLES);
    }

    public static Map<String, Object> extractVariables(HttpServletRequest request, ActionForm actionForm, Interaction interaction,
            IVariableProvider variableProvider) {
        Map<String, String> errors = Maps.newHashMap();
        Map<String, Object> userInput = Maps.newHashMap(actionForm.getMultipartRequestHandler().getAllElements());
        userInput.putAll(getUserInputFiles(request, request.getParameter("id")));
        Map<String, Object> variables = extractVariables(request, interaction, variableProvider, userInput, errors);
        request.setAttribute(USER_DEFINED_VARIABLES, variables);
        if (errors.size() > 0) {
            throw new VariablesFormatException(errors.keySet());
        }
        log.debug("Submitted: " + variables);
        return variables;
    }

    public static Object extractVariable(HttpServletRequest request, ActionForm actionForm, VariableDefinition variableDefinition) throws Exception {
        Map<String, String> formatErrorsForFields = Maps.newHashMap();
        Map<String, Object> inputs = Maps.newHashMap(actionForm.getMultipartRequestHandler().getAllElements());
        inputs.putAll(getUserInputFiles(request, request.getParameter("id")));
        Object variableValue = extractVariable(request, inputs, variableDefinition, formatErrorsForFields);
        if (formatErrorsForFields.size() > 0) {
            throw new VariablesFormatException(formatErrorsForFields.keySet());
        }
        if (!Objects.equal(IGNORED_VALUE, variableValue)) {
            return variableValue;
        }
        return variableValue;
    }

    private static Map<String, Object> extractVariables(HttpServletRequest request, Interaction interaction, IVariableProvider variableProvider,
            Map<String, ? extends Object> userInput, Map<String, String> errors) {
        try {
            User user = Commons.getUser(request.getSession());
            FormComponentExtractionModel model = new FormComponentExtractionModel(variableProvider, user, new RequestWebHelper(request));
            if (interaction.getFormData() != null) {
                String template = new String(interaction.getFormData(), Charsets.UTF_8);
                FreemarkerProcessor.process(template, model);
            }
            HashMap<String, Object> variables = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                try {
                    FormComponentSubmissionHandler handler = model.getSubmissionHandlers().get(variableDefinition.getName());
                    if (handler != null) {
                        variables.putAll(handler.extractVariables(interaction, variableDefinition, userInput, errors));
                    } else {
                        Object variableValue = extractVariable(request, userInput, variableDefinition, errors);
                        if (!Objects.equal(IGNORED_VALUE, variableValue)) {
                            FormComponentSubmissionPostProcessor postProcessor = model.getSubmissionPostProcessors()
                                    .get(variableDefinition.getName());
                            if (postProcessor != null) {
                                variableValue = postProcessor.postProcessValue(variableValue);
                            }
                            variables.put(variableDefinition.getName(), variableValue);
                        }
                    }
                } catch (Exception e) {
                    errors.put(variableDefinition.getName(), e.getMessage());
                    log.warn(variableDefinition.getName(), e);
                }
            }
            return variables;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private static Object extractVariable(HttpServletRequest request, Map<String, ? extends Object> userInput, VariableDefinition variableDefinition,
            Map<String, String> errors) throws Exception {
        VariableFormat format = FormatCommons.create(variableDefinition);
        HttpFormToVariableValue httpFormToVariableValue = new HttpFormToVariableValue(userInput, new DelegateExecutorLoader(Commons.getUser(request
                .getSession())));
        Object result = format.processBy(httpFormToVariableValue, variableDefinition);
        errors.putAll(httpFormToVariableValue.getErrors());
        return result;
    }

    private static Map<String, UploadedFile> getUserInputFiles(HttpServletRequest request) {
        Map<String, UploadedFile> map = (Map<String, UploadedFile>) request.getSession().getAttribute(USER_INPUT_FILES);
        if (map == null) {
            map = Maps.newHashMap();
            request.getSession().setAttribute(USER_INPUT_FILES, map);
        }
        return map;
    }

}
