package ru.runa.wf.web.ftl.component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Objects;

import ru.runa.common.WebResources;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.commons.web.WebUtils;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.IFileVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.GroupFormat;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableDisplaySupport;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatVisitor;
import ru.runa.wfe.var.format.VariableInputSupport;

public class GenerateHtmlForVariable implements VariableFormatVisitor<GenerateHtmlForVariableResult, GenerateHtmlForVariableContext> {

    final User user;
    final WebHelper webHelper;

    public GenerateHtmlForVariable(User user, WebHelper webHelper) {
        super();
        this.user = user;
        this.webHelper = webHelper;
    }

    @Override
    public GenerateHtmlForVariableResult onDate(DateFormat dateFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"text\" name=\"").append(variableName).append("\" class=\"inputDate\" style=\"width: 100px;\" ");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value instanceof Date) {
            html.append("value=\"").append(CalendarUtil.formatDate((Date) value)).append("\" ");
        }
        html.append("/>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onTime(TimeFormat timeFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"text\" name=\"").append(variableName).append("\" class=\"inputTime\" style=\"width: 50px;\" ");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value instanceof Date) {
            html.append("value=\"").append(CalendarUtil.formatTime((Date) value)).append("\" ");
        }
        html.append("/>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onDateTime(DateTimeFormat dateTimeFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"text\" name=\"").append(variableName).append("\" class=\"inputDateTime\" style=\"width: 150px;\" ");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value instanceof Date) {
            html.append("value=\"").append(CalendarUtil.formatDateTime((Date) value)).append("\" ");
        }
        html.append("/>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult OnExecutor(ExecutorFormat executorFormat, GenerateHtmlForVariableContext context) {
        return new GenerateHtmlForVariableResult(
                createExecutorSelect(user, context.variable.getDefinition().getName(), executorFormat, context.variable.getValue(), true), null);
    }

    @Override
    public GenerateHtmlForVariableResult onBoolean(BooleanFormat booleanFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"checkbox\" name=\"").append(variableName).append("\" class=\"inputBoolean\" ");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value instanceof Boolean && (Boolean) value) {
            html.append("checked=\"checked\" ");
        }
        html.append("/>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onBigDecimal(BigDecimalFormat bigDecimalFormat, GenerateHtmlForVariableContext context) {
        return generateNumberHtml(context);
    }

    @Override
    public GenerateHtmlForVariableResult onDouble(DoubleFormat doubleFormat, GenerateHtmlForVariableContext context) {
        return generateNumberHtml(context);
    }

    @Override
    public GenerateHtmlForVariableResult onLong(LongFormat longFormat, GenerateHtmlForVariableContext context) {
        return generateNumberHtml(context);
    }

    @Override
    public GenerateHtmlForVariableResult onFile(FileFormat fileFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        return new GenerateHtmlForVariableResult(getFileComponent(webHelper, variableName, (IFileVariable) value, !context.isReadonly), null);
    }

    @Override
    public GenerateHtmlForVariableResult onHidden(HiddenFormat hiddenFormat, GenerateHtmlForVariableContext context) {
        return new GenerateHtmlForVariableResult(hiddenFormat.getInputHtml(user, webHelper, context.variable), null);
    }

    @Override
    public GenerateHtmlForVariableResult onList(ListFormat listFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        String scriptingVariableName = context.variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat componentFormat = FormatCommons.createComponent(context.variable, 0);
        WfVariable templateComponentVariable = ViewUtil.createListComponentVariable(context.variable, -1, componentFormat, null);
        StringBuffer html = new StringBuffer();
        StringBuffer supportJs = new StringBuffer();
        if (!context.isReadonly) {
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("VARIABLE", variableName);
            substitutions.put("UNIQUENAME", scriptingVariableName);
            GenerateHtmlForVariableResult generatedComponentHtmlData = componentFormat.processBy(this, context.CopyFor(templateComponentVariable));
            String componentHtml = generatedComponentHtmlData.htmlStructureContent;
            String componentJs = generatedComponentHtmlData.scriptContent;
            componentHtml = componentHtml.replaceAll("\"", "'").replaceAll("\t", "").replaceAll("\n", "");
            substitutions.put("COMPONENT_INPUT", componentHtml);
            substitutions.put("COMPONENT_JS_HANDLER", ViewUtil.getComponentJSFunction(templateComponentVariable));
            InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/ViewUtil.EditList.js", ViewUtil.class);
            // html.append(componentHtml);
            supportJs.append(componentJs).append(WebUtils.getFormComponentScript(javascriptStream, substitutions));
        }
        List<Object> list = TypeConversionUtil.convertTo(List.class, context.variable.getValue());
        if (list == null) {
            list = new ArrayList<Object>();
        }
        String listClass = context.isReadonly ? "viewList" : "editList";
        html.append("<div class=\"").append(listClass).append("\" id=\"").append(scriptingVariableName).append("\">");
        if (!context.isReadonly) {
            WfVariable indexesVariable = ViewUtil.createListIndexesVariable(context.variable, list.size());
            html.append(ViewUtil.getHiddenInput(indexesVariable));
            html.append("<div wfeContainerTemplate style=\"display:none\" name=\"").append(variableName).append("\">");
            GenerateHtmlForVariableResult componentGeneratedHtml = componentFormat.processBy(this, context.CopyFor(templateComponentVariable));
            html.append(componentGeneratedHtml.htmlStructureContent);
            html.append("<input type='button' value=' - ' onclick=\"remove").append(scriptingVariableName);
            html.append("(this);\" style=\"width: 30px;\" /></div>");
        }
        for (int row = 0; row < list.size(); row++) {
            if (!context.isReadonly) {
                Object o = list.get(row);
                html.append("<div><div current row=\"").append(row).append("\" name=\"").append(variableName).append("\">");
                WfVariable componentVariable = ViewUtil.createListComponentVariable(context.variable, row, componentFormat, o);
                GenerateHtmlForVariableResult componentGeneratedHtml = componentFormat.processBy(this, context.CopyFor(componentVariable));
                html.append(componentGeneratedHtml.htmlStructureContent);
                html.append("<input type='button' value=' - ' onclick=\"remove").append(scriptingVariableName);
                html.append("(this);\" style=\"width: 30px;\" />");
                html.append("</div></div>");
                supportJs.append(componentGeneratedHtml.scriptContent);
            } else {
                Object listValue = list.get(row);
                html.append("<div row=\"").append(row).append("\" name=\"").append(variableName).append("\">");
                WfVariable componentVariable = ViewUtil.createListComponentVariable(context.variable, row, componentFormat, listValue);
                GenerateHtmlForVariableResult componentGeneratedHtml = componentFormat.processBy(this, context.CopyFor(componentVariable));
                html.append(componentGeneratedHtml.htmlStructureContent);
                html.append("</div>");
                supportJs.append(componentGeneratedHtml.scriptContent);
            }
        }
        if (!context.isReadonly) {
            html.append("<div>");
            html.append("<input type=\"button\" id=\"btnAdd").append(scriptingVariableName);
            html.append("\" value=\" + \" style=\"width: 30px;\" />");
            html.append("</div>");
        }
        html.append("</div>");
        return new GenerateHtmlForVariableResult(html.toString(), supportJs.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onMap(MapFormat mapFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        String scriptingVariableName = context.variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat keyFormat = FormatCommons.createComponent(context.variable, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(context.variable, 1);
        WfVariable templateComponentVariableKey = ViewUtil.createMapKeyComponentVariable(context.variable, -1, null);
        WfVariable templateComponentVariableValue = ViewUtil.createMapValueComponentVariable(context.variable, -1, null);
        StringBuffer html = new StringBuffer();
        StringBuffer supportJs = new StringBuffer();
        if (!context.isReadonly) {
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("VARIABLE", variableName);
            substitutions.put("UNIQUENAME", scriptingVariableName);
            GenerateHtmlForVariableResult generatedKeyHtmlData = keyFormat.processBy(this, context.CopyFor(templateComponentVariableKey));
            String keyComponentHtml = generatedKeyHtmlData.htmlStructureContent;
            String keyComponentJs = generatedKeyHtmlData.scriptContent;
            keyComponentHtml = keyComponentHtml.replaceAll("\"", "'").replaceAll("\t", "").replaceAll("\n", "");
            substitutions.put("COMPONENT_INPUT_KEY", keyComponentHtml);
            GenerateHtmlForVariableResult generatedValueHtmlData = valueFormat.processBy(this, context.CopyFor(templateComponentVariableValue));
            String valueComponentHtml = generatedValueHtmlData.htmlStructureContent;
            String valueComponentJs = generatedValueHtmlData.scriptContent;
            valueComponentHtml = valueComponentHtml.replaceAll("\"", "'").replaceAll("\t", "").replaceAll("\n", "");
            substitutions.put("COMPONENT_INPUT_VALUE", valueComponentHtml);
            String keyJsHandler = keyFormat.processBy(new GenerateJSFunctionsForVariable(), templateComponentVariableKey);
            String valueJsHandler = valueFormat.processBy(new GenerateJSFunctionsForVariable(), templateComponentVariableValue);
            String jsHandler = keyJsHandler + "\n" + valueJsHandler;
            substitutions.put("COMPONENT_JS_HANDLER", jsHandler);
            InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/ViewUtil.EditMap.js", ViewUtil.class);
            supportJs.append(keyComponentJs).append(valueComponentJs).append(WebUtils.getFormComponentScript(javascriptStream, substitutions));
        }
        Map<Object, Object> map = TypeConversionUtil.convertTo(Map.class, context.variable.getValue());
        if (map == null) {
            map = new HashMap<Object, Object>();
        }
        String mapClass = context.isReadonly ? "viewList" : "editList";
        html.append("<div class=\"").append(mapClass).append("\" id=\"").append(scriptingVariableName).append("\">");
        if (!context.isReadonly) {
            WfVariable indexesVariable = ViewUtil.createListIndexesVariable(context.variable, map.size());
            html.append(ViewUtil.getHiddenInput(indexesVariable));
            html.append("<div wfeContainerTemplate style=\"display:none\" name=\"").append(variableName).append("\">");
            GenerateHtmlForVariableResult componentGeneratedHtmlKey = keyFormat.processBy(this, context.CopyFor(templateComponentVariableKey));
            html.append(componentGeneratedHtmlKey.htmlStructureContent);
            GenerateHtmlForVariableResult componentGeneratedHtmlValue = valueFormat.processBy(this, context.CopyFor(templateComponentVariableValue));
            html.append(componentGeneratedHtmlValue.htmlStructureContent);
            html.append("<input type='button' value=' - ' onclick=\"remove").append(scriptingVariableName);
            html.append("(this);\" style=\"width: 30px;\" /></div>");
            int row = -1;
            for (Object key : map.keySet()) {
                row++;
                html.append("<div><div current row=\"").append(row).append("\" name=\"").append(variableName).append("\">");
                WfVariable keyComponentVariable = ViewUtil.createMapKeyComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult keyGeneratedHtml = keyFormat.processBy(this, context.CopyFor(keyComponentVariable));
                html.append(keyGeneratedHtml.htmlStructureContent);
                WfVariable valueComponentVariable = ViewUtil.createMapValueComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult valueGeneratedHtml = valueFormat.processBy(this, context.CopyFor(valueComponentVariable));
                html.append(valueGeneratedHtml.htmlStructureContent);
                html.append("<input type='button' value=' - ' onclick=\"remove").append(scriptingVariableName);
                html.append("(this);\" style=\"width: 30px;\" />");
                html.append("</div></div>");
                supportJs.append(keyGeneratedHtml.scriptContent);
                supportJs.append(valueGeneratedHtml.scriptContent);
            }
            html.append("<div><input type=\"button\" id=\"btnAddMap").append(scriptingVariableName);
            html.append("\" value=\" + \" style=\"width: 30px;\" /></div>");
        } else {
            html.append("<table class=\"list\">");
            if (map != null) {
                int row = -1;
                for (Object key : map.keySet()) {
                    row++;
                    html.append("<tr><td class=\"list\">");
                    html.append("<div row=\"").append(row).append("\" name=\"").append(variableName).append("\">");
                    WfVariable keyComponentVariable = ViewUtil.createMapKeyComponentVariable(context.variable, row, key);
                    GenerateHtmlForVariableResult keyGeneratedHtml = keyFormat.processBy(this, context.CopyFor(keyComponentVariable));
                    html.append(keyGeneratedHtml.htmlStructureContent);
                    html.append("</td><td class=\"list\">");
                    WfVariable valueComponentVariable = ViewUtil.createMapValueComponentVariable(context.variable, row, key);
                    GenerateHtmlForVariableResult valueGeneratedHtml = valueFormat.processBy(this, context.CopyFor(valueComponentVariable));
                    html.append(valueGeneratedHtml.htmlStructureContent);
                    html.append("</td></div></tr>");
                    supportJs.append(keyGeneratedHtml.scriptContent);
                    supportJs.append(valueGeneratedHtml.scriptContent);
                }
            }
            html.append("</table>");
        }
        html.append("</div>");
        return new GenerateHtmlForVariableResult(html.toString(), supportJs.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onProcessId(ProcessIdFormat processIdFormat, GenerateHtmlForVariableContext context) {
        return generateNumberHtml(context);
    }

    @Override
    public GenerateHtmlForVariableResult onString(StringFormat stringFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"text\" name=\"").append(variableName).append("\" class=\"inputString\" ");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value != null) {
            html.append("value=\"").append(stringFormat.formatHtml(user, webHelper, context.processId, variableName, value)).append("\" ");
        }
        html.append("/>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onTextString(TextFormat textFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<textarea name=\"").append(variableName).append("\" class=\"inputText\">");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value != null) {
            html.append(textFormat.formatHtml(user, webHelper, context.processId, variableName, value));
        }
        html.append("</textarea>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onUserType(UserTypeFormat userTypeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        UserType userType = userTypeFormat.getUserType();
        UserTypeMap userTypeMap = (UserTypeMap) value;
        if (userTypeMap == null) {
            userTypeMap = new UserTypeMap(userType);
        }
        StringBuffer html = new StringBuffer();
        StringBuffer supportJs = new StringBuffer();
        html.append("<table class=\"list\">");
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            html.append("<tr>");
            html.append("<td class=\"list\">").append(attributeDefinition.getName()).append("</td>");
            html.append("<td class=\"list\">");
            Object attributeValue = userTypeMap.get(attributeDefinition.getName());
            WfVariable componentVariable = ViewUtil.createUserTypeComponentVariable(context.variable, attributeDefinition, attributeValue);
            GenerateHtmlForVariableResult attributeGeneratedHtml = componentVariable.getDefinition().getFormatNotNull().processBy(this,
                    new GenerateHtmlForVariableContext(componentVariable, context.processId, context.isReadonly));
            html.append(attributeGeneratedHtml.htmlStructureContent);
            html.append("</td>");
            html.append("</tr>");
            supportJs.append(attributeGeneratedHtml.scriptContent);
        }
        html.append("</table>");
        return new GenerateHtmlForVariableResult(html.toString(), supportJs.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onOther(VariableFormat variableFormat, GenerateHtmlForVariableContext context) {
        WfVariable variable = context.variable;
        if (!context.isReadonly) {
            if (variableFormat instanceof VariableInputSupport) {
                return new GenerateHtmlForVariableResult(((VariableInputSupport) variableFormat).getInputHtml(user, webHelper, variable), null);
            } else {
                throw new InternalApplicationException("No input method implemented for " + variableFormat);
            }
        } else {
            if (variableFormat instanceof VariableDisplaySupport) {
                VariableDisplaySupport displaySupport = (VariableDisplaySupport) variableFormat;
                return new GenerateHtmlForVariableResult(
                        displaySupport.formatHtml(user, webHelper, context.processId, variable.getDefinition().getName(), variable.getValue()), null);
            }
            throw new InternalApplicationException("No output method implemented for " + variableFormat);
        }
    }

    public static String createExecutorSelect(User user, WfVariable variable) {
        return createExecutorSelect(user, variable.getDefinition().getName(), variable.getDefinition().getFormatNotNull(), variable.getValue(), true);
    }

    private static String createExecutorSelect(User user, String variableName, VariableFormat variableFormat, Object value, boolean enabled) {
        BatchPresentation batchPresentation;
        int sortColumn = 0;
        boolean javaSort = false;
        if (ActorFormat.class == variableFormat.getClass()) {
            batchPresentation = BatchPresentationFactory.ACTORS.createNonPaged();
            sortColumn = 1;
        } else if (ExecutorFormat.class == variableFormat.getClass()) {
            batchPresentation = BatchPresentationFactory.EXECUTORS.createNonPaged();
            javaSort = true;
        } else if (GroupFormat.class == variableFormat.getClass()) {
            batchPresentation = BatchPresentationFactory.GROUPS.createNonPaged();
        } else {
            throw new InternalApplicationException("Unexpected format " + variableFormat);
        }
        batchPresentation.setFieldsToSort(new int[] { sortColumn }, new boolean[] { true });
        List<Executor> executors = (List<Executor>) Delegates.getExecutorService().getExecutors(user, batchPresentation);
        return createExecutorSelect(variableName, executors, value, javaSort, enabled);
    }

    public static String createExecutorSelect(String variableName, List<? extends Executor> executors, Object value, boolean javaSort,
            boolean enabled) {
        String html = "<select name=\"" + variableName + "\"";
        if (!enabled) {
            html += " disabled=\"true\"";
        }
        html += ">";
        if (javaSort) {
            Collections.sort(executors);
        }
        html += "<option value=\"\"> ------------------------- </option>";
        for (Executor executor : executors) {
            html += "<option value=\"ID" + executor.getId() + "\"";
            if (Objects.equal(executor, value)) {
                html += " selected";
            }
            html += ">" + executor.getLabel() + "</option>";
        }
        html += "</select>";
        return html;
    }

    public static String getFileComponent(WebHelper webHelper, String variableName, IFileVariable value, boolean enabled) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" />";
        }
        String id = null;
        UploadedFile file = null;
        if (webHelper != null) {
            // TODO: taskId transmit to the method
            id = webHelper.getRequest().getParameter("id");
            if (id == null) {
                throw new InternalApplicationException("id not found");
            }
            file = FormSubmissionUtils.getUploadedFilesMap(webHelper.getRequest()).get(id + FormSubmissionUtils.FILES_MAP_QUALIFIER + variableName);
            if (value != null && file == null) {
                file = new UploadedFile(value);
                if (enabled) {
                    // #766, load file content only for input file component
                    file.setContent(value.getData());
                }
                String fileKey = id + FormSubmissionUtils.FILES_MAP_QUALIFIER + variableName;
                FormSubmissionUtils.getUploadedFilesMap(webHelper.getRequest()).put(fileKey, file);
            }
        }
        String attachImageUrl = "";
        String loadingImageUrl = "";
        String deleteImageUrl = "";
        String uploadFileTitle = "Upload file";
        String loadingMessage = "Loading ...";
        if (webHelper != null) {
            attachImageUrl = webHelper.getUrl(Resources.IMAGE_ATTACH);
            loadingImageUrl = webHelper.getUrl(Resources.IMAGE_LOADING);
            deleteImageUrl = webHelper.getUrl(Resources.IMAGE_DELETE);
            uploadFileTitle = webHelper.getMessage("message.upload.file");
            loadingMessage = webHelper.getMessage("message.loading");
        }
        String hideStyle = "style=\"display: none;\"";
        String html = "<div class=\"inputFileContainer\"" + (!enabled && file == null ? hideStyle : "") + ">";
        html += "<div class=\"dropzone\" " + (file != null ? hideStyle : "") + ">";
        html += "<label class=\"inputFileAttach\">";
        html += "<div class=\"inputFileAttachButtonDiv\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "<input class=\"inputFile inputFileAjax\" name=\"" + variableName + "\" type=\"file\"" + (enabled ? "current" : "") + ">";
        html += "</label></div>";
        html += "<div class=\"progressbar\" " + (file == null ? hideStyle : "") + ">";
        html += "<div class=\"line\" style=\"width: " + (file != null ? "10" : "") + "0%;\"></div>";
        html += "<div class=\"status\">";
        if (enabled) {
            if (file != null) {
                html += "<img src=\"" + deleteImageUrl + "\" class=\"inputFileDelete\" inputId=\"" + variableName + "\">";
            } else {
                html += "<img src=\"" + loadingImageUrl + "\" inputId=\"" + variableName + "\">";
            }
        }

        html += "<span class=\"statusText\">";
        if (file != null && webHelper != null) {
            String viewUrl = webHelper.getUrl("/upload?action=view&inputId=" + variableName + "&id=" + id);
            html += "<a href='" + viewUrl + "'>" + file.getName() + (file.getSize() != null ? " - " + file.getSize() : "") + "</a>";
        } else {
            html += loadingMessage;
        }
        html += "</span></div></div></div>";
        return html;
    }

    private GenerateHtmlForVariableResult generateNumberHtml(GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"text\" name=\"").append(variableName).append("\" class=\"inputNumber\" ");
        if (context.isReadonly) {
            html.append("disabled=\"true\" ");
        }
        if (value instanceof Number) {
            html.append("value=\"").append(value).append("\" ");
        }
        html.append("/>");
        return new GenerateHtmlForVariableResult(html.toString(), null);
    }
}
