package ru.runa.wf.web.ftl.component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class ViewUtil {
    private static final Log log = LogFactory.getLog(ViewUtil.class);

    public static String createExecutorSelect(User user, WfVariable variable) {
        return GenerateHtmlForVariable.createExecutorSelect(user, variable);
    }

    public static String createExecutorSelect(String variableName, List<? extends Executor> executors, Object value, boolean javaSort, boolean enabled) {
        return GenerateHtmlForVariable.createExecutorSelect(variableName, executors, value, javaSort, enabled);
    }

    public static WfVariable createVariable(String variableName, String scriptingName, VariableFormat componentFormat, Object value) {
        VariableDefinition definition = new VariableDefinition(variableName, scriptingName, componentFormat);
        return new WfVariable(definition, value);
    }

    public static WfVariable createComponentVariable(WfVariable containerVariable, String nameSuffix, VariableFormat componentFormat, Object value) {
        String name = containerVariable.getDefinition().getName() + (nameSuffix != null ? nameSuffix : "");
        String scriptingName = containerVariable.getDefinition().getScriptingName() + (nameSuffix != null ? nameSuffix : "");
        return createVariable(name, scriptingName, componentFormat, value);
    }

    public static WfVariable createListComponentVariable(WfVariable containerVariable, int index, VariableFormat componentFormat, Object value) {
        String nameSuffix = VariableFormatContainer.COMPONENT_QUALIFIER_START;
        if (index != -1) {
            nameSuffix += index;
        }
        nameSuffix += VariableFormatContainer.COMPONENT_QUALIFIER_END;
        return createComponentVariable(containerVariable, nameSuffix, componentFormat, value);
    }

    public static WfVariable createMapComponentVariable(WfVariable mapVariable, Object key) {
        Map<?, ?> map = (Map<?, ?>) mapVariable.getValue();
        Object object = map.get(key);
        VariableFormat keyFormat = FormatCommons.createComponent(mapVariable, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(mapVariable, 1);
        String nameSuffix = VariableFormatContainer.COMPONENT_QUALIFIER_START;
        String keyString = key != null ? keyFormat.format(key) : null;
        nameSuffix += Strings.isNullOrEmpty(keyString) ? MapFormat.KEY_NULL_VALUE : keyString;
        nameSuffix += VariableFormatContainer.COMPONENT_QUALIFIER_END;
        return createComponentVariable(mapVariable, nameSuffix, valueFormat, object);
    }

    public static WfVariable createMapKeyComponentVariable(WfVariable mapVariable, int index, Object key) {
        VariableFormat keyFormat = FormatCommons.createComponent(mapVariable, 0);
        String nameSuffix = VariableFormatContainer.COMPONENT_QUALIFIER_START;
        if (index != -1) {
            nameSuffix += index;
        }
        nameSuffix += VariableFormatContainer.COMPONENT_QUALIFIER_END;
        return createComponentVariable(mapVariable, nameSuffix + ".key", keyFormat, key);
    }

    public static WfVariable createMapValueComponentVariable(WfVariable mapVariable, int index, Object key) {
        Map<?, ?> map = (Map<?, ?>) mapVariable.getValue();
        Object value = key != null ? map.get(key) : null;
        VariableFormat valueFormat = FormatCommons.createComponent(mapVariable, 1);
        String nameSuffix = VariableFormatContainer.COMPONENT_QUALIFIER_START;
        if (index != -1) {
            nameSuffix += index;
        }
        nameSuffix += VariableFormatContainer.COMPONENT_QUALIFIER_END;
        return createComponentVariable(mapVariable, nameSuffix + ".value", valueFormat, value);
    }

    public static WfVariable createListIndexesVariable(WfVariable containerVariable, Object value) {
        return createComponentVariable(containerVariable, FormSubmissionUtils.INDEXES_SUFFIX, new StringFormat(), value);
    }

    public static WfVariable createUserTypeComponentVariable(WfVariable containerVariable, VariableDefinition attributeDefinition, Object value) {
        String name = containerVariable.getDefinition().getName() + UserType.DELIM + attributeDefinition.getName();
        String scriptingName = containerVariable.getDefinition().getScriptingName() + UserType.DELIM + attributeDefinition.getScriptingName();
        return createVariable(name, scriptingName, attributeDefinition.getFormatNotNull(), value);
    }

    public static String getHiddenInput(WfVariable variable) {
        String stringValue = variable.getStringValue();
        if (stringValue == null) {
            stringValue = "";
        }
        return "<input type=\"hidden\" name=\"" + variable.getDefinition().getName() + "\" value=\"" + stringValue + "\" />";
    }

    public static String getFileInput(WebHelper webHelper, String variableName, boolean allowMultiple) {
        String multiple = allowMultiple ? " multiple " : "";
        String attachImageUrl = "";
        String loadingImageUrl = "";
        String uploadFileTitle = webHelper.getMessage("message.upload.file");
        String loadingMessage = webHelper.getMessage("message.loading");
        attachImageUrl = webHelper.getUrl(Resources.IMAGE_ATTACH);
        loadingImageUrl = webHelper.getUrl(Resources.IMAGE_LOADING);
        loadingMessage = webHelper.getMessage("message.loading");
        String hideStyle = "style=\"display: none;\"";
        String html = "<div class=\"inputFileContainer\">";
        html += "<div class=\"dropzone\" >";
        html += "<label class=\"inputFileAttach\">";
        html += "<div class=\"inputFileAttachButtonDiv\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "<input class=\"inputFile inputFileAjax \" name=\"" + variableName + "\" type=\"file\" " + multiple + ">";
        html += "</label></div>";
        html += "<div class=\"progressbar\" " + hideStyle + ">";
        html += "<div class=\"line\" style=\"width: 0%;\"></div>";
        html += "<div class=\"status\">";
        html += "<img src=\"" + loadingImageUrl + "\" inputId=\"" + variableName + "\">";
        html += "<span class=\"statusText\">";
        html += loadingMessage;
        html += "</span></div></div></div>";
        return html;
    }

    public static String getComponentInput(User user, WebHelper webHelper, WfVariable variable) {
        VariableFormat variableFormat = variable.getDefinition().getFormatNotNull();
        GenerateHtmlForVariableContext context = new GenerateHtmlForVariableContext(variable, 0L, false);
        GenerateHtmlForVariableResult generatedResult = variableFormat.processBy(new GenerateHtmlForVariable(user, webHelper), context);
        return generatedResult.htmlStructureContent + "\n" + generatedResult.scriptContent;
    }

    public static String getComponentOutput(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        VariableFormat variableFormat = variable.getDefinition().getFormatNotNull();
        GenerateHtmlForVariableContext context = new GenerateHtmlForVariableContext(variable, processId, true);
        GenerateHtmlForVariableResult generatedResult = variableFormat.processBy(new GenerateHtmlForVariable(user, webHelper), context);
        return generatedResult.htmlStructureContent + "\n" + generatedResult.scriptContent;
    }

    public static String getComponentJSFunction(WfVariable variable) {
        VariableFormat variableFormat = variable.getDefinition().getFormatNotNull();
        return variableFormat.processBy(new GenerateJSFunctionsForVariable(), variable);
    }

    public static String getOutput(User user, WebHelper webHelper, Long processId, WfVariable variable) {
        try {
            if (variable.getValue() == null) {
                return "";
            }
            VariableFormat format = variable.getDefinition().getFormatNotNull();
            if (format instanceof HiddenFormat) {
                // display in admin interface
                return format.format(variable.getValue());
            }
            if (format instanceof VariableDisplaySupport) {
                VariableDisplaySupport displaySupport = (VariableDisplaySupport) format;
                return displaySupport.formatHtml(user, webHelper, processId, variable.getDefinition().getName(), variable.getValue());
            } else {
                return format.format(variable.getValue());
            }
        } catch (Exception e) {
            log.warn("Unable to format value " + variable + " in " + processId + ": " + e.getMessage());
            if (variable.getValue() != null && variable.getValue().getClass().isArray()) {
                return Arrays.toString((Object[]) variable.getValue());
            } else {
                if (variable.getDefinition().isSynthetic()) {
                    return String.valueOf(variable.getValue());
                } else {
                    return " <span style=\"color: #cccccc;\">(" + variable.getValue() + ")</span>";
                }
            }
        }
    }

    public static String generateTableHeader(List<WfVariable> variables, IVariableProvider variableProvider, String operationsColumn) {
        StringBuffer header = new StringBuffer();
        header.append("<tr class=\"header\">");
        for (WfVariable variable : variables) {
            Object value = variableProvider.getValue(variable.getDefinition().getName() + "_header");
            if (value == null) {
                value = variable.getDefinition().getName();
            }
            header.append("<th>").append(value).append("</th>");
        }
        if (operationsColumn != null) {
            header.append(operationsColumn);
        }
        header.append("</tr>");
        return header.toString();
    }

    public static String getFileLogOutput(WebHelper webHelper, Long logId, String fileName) {
        HashMap<String, Object> params = Maps.newHashMap();
        params.put(WebHelper.PARAM_ID, logId);
        String href = webHelper.getActionUrl(WebHelper.ACTION_DOWNLOAD_LOG_FILE, params);
        return "<a href=\"" + href + "\">" + fileName + "</>";
    }

    public static String wrapInputVariable(WfVariable variable, String componentHtml) {
        String tagToUse = "span";
        if (HTMLUtils.checkForBlockElements(componentHtml)) {
            tagToUse = "div";
        }
        String html = "<" + tagToUse + " class=\"inputVariable " + variable.getDefinition().getScriptingNameWithoutDots() + "\"";
        html += " variable='" + variable.getDefinition().getName() + "'>";
        html += componentHtml;
        html += "</" + tagToUse + ">";
        return html;
    }

    public static String wrapDisplayVariable(WfVariable variable, String componentHtml) {
        String tagToUse = "span";
        if (HTMLUtils.checkForBlockElements(componentHtml)) {
            tagToUse = "div";
        }
        String html = "<" + tagToUse + " class=\"displayVariable " + variable.getDefinition().getScriptingNameWithoutDots() + "\"";
        html += " variable='" + variable.getDefinition().getName() + "'>";
        html += componentHtml;
        html += "</" + tagToUse + ">";
        return html;
    }

}
