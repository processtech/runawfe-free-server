package ru.runa.wf.web;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

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
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class FormSubmissionUtils {
    private static final Log log = LogFactory.getLog(FormSubmissionUtils.class);
    public static final String USER_DEFINED_VARIABLES = "UserDefinedVariables";
    public static final String USER_ERRORS = "UserErrors";
    public static final String TASK_UPLOADED_FILES = "TaskUploadedFiles";
    public static final Object IGNORED_VALUE = new Object();
    public static final String INDEXES_SUFFIX = ".indexes";
    public static final String FILES_MAP_QUALIFIER = ":";
    public static final String FORM_NODE_ID_KEY = "UserDefinedVariablesForFormNodeId";

    /**
     * save in request user input with errors
     * 
     * @param errors
     *            validation errors
     */
    public static void saveUserFormInput(HttpServletRequest request, ActionForm form, Map<String, String> errors) {
        request.setAttribute(USER_DEFINED_VARIABLES, extractAllAvailableVariables(form));
        // save in request user errors
        request.setAttribute(USER_ERRORS, errors);
    }

    /**
     * @return saved in request values from previous form submit (used to re-open form in case of validation errors)
     */
    public static Map<String, String[]> getUserFormInput(ServletRequest request) {
        return (Map<String, String[]>) request.getAttribute(USER_DEFINED_VARIABLES);
    }

    /**
     * @return saved in request values from previous form submit (used to re-open form in case of validation errors)
     */
    public static Map<String, Object> getUserFormInputVariables(HttpServletRequest request, Interaction interaction,
            IVariableProvider variableProvider) {
        Map<String, String[]> userInput = getUserFormInput(request);
        if (userInput != null && Objects.equal(userInput.get(FORM_NODE_ID_KEY)[0], interaction.getNodeId())) {
            Map<String, String> errors = Maps.newHashMap();
            return extractVariables(request, interaction, variableProvider, userInput, errors);
        }
        return null;
    }

    public static Map<String, String> getUserFormValidationErrors(ServletRequest request) {
        Map<String, String> map = (Map<String, String>) request.getAttribute(USER_ERRORS);
        if (map == null) {
            map = Maps.newHashMap();
        }
        return map;
    }

    private static Map<String, String[]> extractAllAvailableVariables(ActionForm actionForm) {
        Hashtable<String, Object> hashtable = actionForm.getMultipartRequestHandler().getAllElements();
        Map<String, String[]> variablesMap = new HashMap<String, String[]>();
        for (Map.Entry<String, Object> entry : hashtable.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof FormFile) {
                // we could not fulfill in future this type of the input on the
                // web page (access restriction), so discard it
                continue;
            } else {
                variablesMap.put(entry.getKey(), (String[]) value);
            }
        }
        return variablesMap;
    }

    public static Map<String, Object> extractVariables(HttpServletRequest request, ActionForm actionForm, Interaction interaction,
            IVariableProvider variableProvider) {
        Map<String, String> errors = Maps.newHashMap();
        Map<String, Object> userInput = Maps.newHashMap(actionForm.getMultipartRequestHandler().getAllElements());
        userInput.putAll(getUploadedFilesInputsMap(request));
        Map<String, Object> variables = extractVariables(request, interaction, variableProvider, userInput, errors);
        if (errors.size() > 0) {
            throw new VariablesFormatException(errors.keySet());
        }
        log.debug("Submitted: " + variables);
        return variables;
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

    public static Object extractVariable(HttpServletRequest request, ActionForm actionForm, VariableDefinition variableDefinition) throws Exception {
        Map<String, String> formatErrorsForFields = Maps.newHashMap();
        Map<String, Object> inputs = Maps.newHashMap(actionForm.getMultipartRequestHandler().getAllElements());
        inputs.putAll(getUploadedFilesInputsMap(request));
        Object variableValue = extractVariable(request, inputs, variableDefinition, formatErrorsForFields);
        if (formatErrorsForFields.size() > 0) {
            throw new VariablesFormatException(formatErrorsForFields.keySet());
        }
        if (!Objects.equal(IGNORED_VALUE, variableValue)) {
            return variableValue;
        }
        return variableValue;
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

    public static Map<String, UploadedFile> getUploadedFilesInputsMap(HttpServletRequest request) {
        Map<String, UploadedFile> inputsMap = Maps.newHashMap();
        for (Entry<String, UploadedFile> entry : getUploadedFilesMap(request).entrySet()) {
            if (entry.getKey().split(FILES_MAP_QUALIFIER)[0].equals(request.getParameter("id"))) {
                inputsMap.put(entry.getKey().split(FILES_MAP_QUALIFIER)[1], entry.getValue());
            }
        }
        return inputsMap;
    }

    public static Map<String, UploadedFile> getUploadedFilesMap(HttpServletRequest request) {
        Map<String, UploadedFile> map = (Map<String, UploadedFile>) request.getSession().getAttribute(TASK_UPLOADED_FILES);
        if (map == null) {
            map = Maps.newHashMap();
            request.getSession().setAttribute(TASK_UPLOADED_FILES, map);
        }
        return map;
    }

}
