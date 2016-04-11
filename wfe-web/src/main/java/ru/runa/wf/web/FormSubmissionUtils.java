package ru.runa.wf.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.upload.FormFile;

import ru.runa.common.WebResources;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionPostProcessor;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
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
     * @return saved in request values from previous form submit (used to
     *         re-open form in case of validation errors)
     */
    public static Map<String, String[]> getUserFormInput(ServletRequest request) {
        return (Map<String, String[]>) request.getAttribute(USER_DEFINED_VARIABLES);
    }

    /**
     * @return saved in request values from previous form submit (used to
     *         re-open form in case of validation errors)
     */
    public static Map<String, Object> getUserFormInputVariables(HttpServletRequest request, Interaction interaction) {
        Map<String, String[]> userInput = getUserFormInput(request);
        if (userInput != null && Objects.equal(userInput.get(FORM_NODE_ID_KEY)[0], interaction.getNodeId())) {
            Map<String, String> errors = Maps.newHashMap();
            return extractVariables(request, interaction, userInput, errors);
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
        for (String varName : hashtable.keySet()) {
            Object value = hashtable.get(varName);
            if (value instanceof FormFile) {
                // we could not fulfill in future this type of the input on the
                // web page (access restriction), so discard it
                continue;
            } else {
                variablesMap.put(varName, (String[]) value);
            }
        }
        return variablesMap;
    }

    public static Map<String, Object> extractVariables(HttpServletRequest request, ActionForm actionForm, Interaction interaction) {
        Map<String, String> errors = Maps.newHashMap();
        Map<String, Object> userInput = Maps.newHashMap(actionForm.getMultipartRequestHandler().getAllElements());
        userInput.putAll(getUploadedFilesInputsMap(request));
        Map<String, Object> variables = extractVariables(request, interaction, userInput, errors);
        if (errors.size() > 0) {
            throw new VariablesFormatException(errors.keySet());
        }
        log.debug("Submitted: " + variables);
        return variables;
    }

    private static Map<String, Object> extractVariables(HttpServletRequest request, Interaction interaction, Map<String, ? extends Object> userInput,
            Map<String, String> errors) {
        try {
            HashMap<String, Object> variables = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                try {
                    FormComponentSubmissionHandler handler = (FormComponentSubmissionHandler) request.getSession().getAttribute(
                            FormComponentSubmissionHandler.KEY_PREFIX + variableDefinition.getName());
                    if (handler != null) {
                        variables.putAll(handler.extractVariables(interaction, variableDefinition, userInput, errors));
                    } else {
                        Object variableValue = extractVariable(userInput, variableDefinition, errors);
                        if (!Objects.equal(IGNORED_VALUE, variableValue)) {
                            FormComponentSubmissionPostProcessor postProcessor = (FormComponentSubmissionPostProcessor) request.getSession()
                                    .getAttribute(FormComponentSubmissionPostProcessor.KEY_PREFIX + variableDefinition.getName());
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
        Object variableValue = extractVariable(inputs, variableDefinition, formatErrorsForFields);
        if (formatErrorsForFields.size() > 0) {
            throw new VariablesFormatException(formatErrorsForFields.keySet());
        }
        if (!Objects.equal(IGNORED_VALUE, variableValue)) {
            return variableValue;
        }
        return variableValue;
    }

    private static Object extractVariable(Map<String, ? extends Object> userInput, VariableDefinition variableDefinition, Map<String, String> errors)
            throws Exception {
        VariableFormat format = FormatCommons.create(variableDefinition);
        Object variableValue = null;
        boolean forceSetVariableValue = false;
        if (format instanceof ListFormat) {
            List<Integer> indexes = null;
            String sizeInputName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
            String indexesInputName = variableDefinition.getName() + INDEXES_SUFFIX;
            ListFormat listFormat = (ListFormat) format;
            VariableFormat componentFormat = FormatCommons.createComponent(variableDefinition, 0);
            List<Object> list = null;
            String[] stringsIndexes = (String[]) userInput.get(indexesInputName);
            if (stringsIndexes == null || stringsIndexes.length != 1) {
                if (userInput.containsKey(sizeInputName)) {
                    // js dynamic way
                    String[] stringsSize = (String[]) userInput.get(sizeInputName);
                    if (stringsSize == null || stringsSize.length != 1) {
                        log.error("Incorrect '" + sizeInputName + "' value submitted: " + Arrays.toString(stringsSize));
                        return null;
                    }
                    int listSize = TypeConversionUtil.convertTo(int.class, stringsSize[0]);
                    list = Lists.newArrayListWithExpectedSize(listSize);
                    indexes = Lists.newArrayListWithExpectedSize(listSize);
                    for (int i = 0; indexes.size() < listSize && i < 1000; i++) {
                        String checkString = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        for (String key : userInput.keySet()) {
                            if (key.startsWith(checkString)) {
                                indexes.add(i);
                                break;
                            }
                        }
                    }
                    if (indexes.size() != listSize) {
                        errors.put(variableDefinition.getName(), ". Not all list items found. Expected:'" + listSize + "', found:'" + indexes.size());
                    }
                    for (Integer index : indexes) {
                        String name = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        String scriptingName = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        VariableDefinition componentDefinition = new VariableDefinition(name, scriptingName, componentFormat);
                        Object componentValue = extractVariable(userInput, componentDefinition, errors);
                        if (!Objects.equal(IGNORED_VALUE, componentValue)) {
                            list.add(componentValue);
                        }
                    }
                    variableValue = list;
                } else {
                    // http old-style way
                    String[] strings = (String[]) userInput.get(variableDefinition.getName());
                    if (strings == null || strings.length == 0) {
                        return null;
                    }
                    list = Lists.newArrayListWithExpectedSize(strings.length);
                    for (String componentValue : strings) {
                        list.add(convertComponent(variableDefinition.getName(), componentFormat, componentValue, errors));
                    }
                    variableValue = list;
                }
            } else {
                int listSize = !stringsIndexes[0].equals("") ? stringsIndexes[0].toString().split(",").length : 0;
                list = Lists.newArrayListWithExpectedSize(listSize);
                if (listSize > 0) {
                    indexes = Lists.newArrayListWithExpectedSize(listSize);
                    String[] stringIndexes = stringsIndexes[0].toString().split(",");
                    for (String index : stringIndexes) {
                        indexes.add(TypeConversionUtil.convertTo(int.class, index));
                    }
                    for (Integer index : indexes) {
                        String name = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        String scriptingName = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                                + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                        VariableDefinition componentDefinition = new VariableDefinition(name, scriptingName, componentFormat);
                        Object componentValue = extractVariable(userInput, componentDefinition, errors);
                        if (!Objects.equal(IGNORED_VALUE, componentValue)) {
                            list.add(componentValue);
                        }
                    }
                }
                variableValue = list;
            }

        } else if (format instanceof MapFormat) {
            Map<Object, Object> map = null;
            String sizeInputName = variableDefinition.getName() + VariableFormatContainer.SIZE_SUFFIX;
            String indexesInputName = variableDefinition.getName() + INDEXES_SUFFIX;
            MapFormat mapFormat = (MapFormat) format;
            VariableFormat componentKeyFormat = FormatCommons.createComponent(variableDefinition, 0);
            VariableFormat componentValueFormat = FormatCommons.createComponent(variableDefinition, 1);
            String[] stringsIndexes = (String[]) userInput.get(indexesInputName);
            List<Integer> indexes = null;
            if (stringsIndexes == null || stringsIndexes.length != 1) {
                String[] stringsSize = (String[]) userInput.get(sizeInputName);
                if (stringsSize == null || stringsSize.length != 1) {
                    log.error("Incorrect '" + sizeInputName + "' value submitted: " + Arrays.toString(stringsSize));
                    return null;
                }
                int mapSize = TypeConversionUtil.convertTo(int.class, stringsSize[0]);
                map = Maps.newHashMapWithExpectedSize(mapSize);
                indexes = Lists.newArrayListWithExpectedSize(mapSize);
                for (int i = 0; indexes.size() < mapSize && i < 1000; i++) {
                    String checkString = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + i
                            + VariableFormatContainer.COMPONENT_QUALIFIER_END;
                    for (String key : userInput.keySet()) {
                        if (key.startsWith(checkString)) {
                            indexes.add(i);
                            break;
                        }
                    }
                }
                if (indexes.size() != mapSize) {
                    errors.put(variableDefinition.getName(), ". Not all list items found. Expected:'" + mapSize + "', found:'" + indexes.size());
                }
            } else {
                int mapSize = !stringsIndexes[0].equals("") ? stringsIndexes[0].toString().split(",").length : 0;
                map = Maps.newHashMapWithExpectedSize(mapSize);
                indexes = Lists.newArrayListWithExpectedSize(mapSize);
                if (mapSize > 0) {
                    String[] stringIndexes = stringsIndexes[0].toString().split(",");
                    for (String index : stringIndexes) {
                        indexes.add(TypeConversionUtil.convertTo(int.class, index));
                    }
                }
            }
            for (Integer index : indexes) {
                String nameKey = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END + ".key";
                String scriptingNameKey = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END + ".key";
                VariableDefinition componentKeyDefinition = new VariableDefinition(nameKey, scriptingNameKey, componentKeyFormat);
                Object componentKeyValue = extractVariable(userInput, componentKeyDefinition, errors);

                String nameValue = variableDefinition.getName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END + ".value";
                String scriptingNameValue = variableDefinition.getScriptingName() + VariableFormatContainer.COMPONENT_QUALIFIER_START + index
                        + VariableFormatContainer.COMPONENT_QUALIFIER_END + ".value";
                VariableDefinition componentValueDefinition = new VariableDefinition(nameValue, scriptingNameValue, componentValueFormat);
                Object componentValueValue = extractVariable(userInput, componentValueDefinition, errors);
                if (!Objects.equal(IGNORED_VALUE, componentKeyValue)) {
                    map.put(componentKeyValue, componentValueValue);
                }
            }
            variableValue = map;
        } else if (format instanceof UserTypeFormat) {
            List<VariableDefinition> expandedDefinitions = variableDefinition.expandUserType(false);
            UserTypeMap userTypeMap = new UserTypeMap(variableDefinition);
            for (VariableDefinition expandedDefinition : expandedDefinitions) {
                Object componentValue = extractVariable(userInput, expandedDefinition, errors);
                if (!Objects.equal(IGNORED_VALUE, componentValue)) {
                    String attributeName = expandedDefinition.getName().substring(variableDefinition.getName().length() + 1);
                    userTypeMap.put(attributeName, componentValue);
                }
            }
            variableValue = userTypeMap;
        } else {
            Object value = userInput.get(variableDefinition.getName());
            if (value != null) {
                forceSetVariableValue = true;
            }
            variableValue = convertComponent(variableDefinition.getName(), format, value, errors);
        }
        if (variableValue != null || forceSetVariableValue) {
            return variableValue;
        }
        return IGNORED_VALUE;
    }

    private static Object convertComponent(String inputName, VariableFormat format, Object value, Map<String, String> formatErrorsForFields) {
        try {
            if (format instanceof BooleanFormat) {
                if (value == null) {
                    // HTTP FORM doesn't pass unchecked checkbox value
                    value = new String[] { Boolean.FALSE.toString() };
                }
            }
            if (value instanceof String[]) {
                value = ((String[]) value)[0];
            }
            if (value instanceof FormFile) {
                FormFile formFile = (FormFile) value;
                if (formFile.getFileSize() > 0) {
                    String contentType = formFile.getContentType();
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    return new FileVariable(formFile.getFileName(), formFile.getFileData(), contentType);
                }
                if (SystemProperties.isV3CompatibilityMode() || !WebResources.isAjaxFileInputEnabled()) {
                    return IGNORED_VALUE;
                }
            } else if (value instanceof UploadedFile) {
                UploadedFile uploadedFile = (UploadedFile) value;
                if (uploadedFile.getFileVariable() instanceof FileVariableProxy) {
                    // for process update value
                    return uploadedFile.getFileVariable();
                }
                if (uploadedFile.getContent() == null) {
                    // null for display component
                    return IGNORED_VALUE;
                }
                return new FileVariable(uploadedFile.getName(), uploadedFile.getContent(), uploadedFile.getMimeType());
            } else if (value instanceof String) {
                String valueToFormat = (String) value;
                try {
                    return format.parse(valueToFormat);
                } catch (Exception e) {
                    log.warn(e);
                    if (valueToFormat.length() > 0) {
                        // in other case we put validation in logic
                        formatErrorsForFields.put(inputName, e.getMessage());
                    }
                }
            } else if (value == null) {
            } else {
                throw new InternalApplicationException("Unexpected class: " + value.getClass());
            }
            return null;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
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
