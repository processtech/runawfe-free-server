package ru.runa.wf.web.ftl.component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ecs.html.Div;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;

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
import ru.runa.wfe.var.format.FormattedTextFormat;
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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Component for generation HTML code for displaying variable or get inputs from usr for variable.
 */
public class GenerateHtmlForVariable implements VariableFormatVisitor<GenerateHtmlForVariableResult, GenerateHtmlForVariableContext> {

    /**
     * User, requested HTML code generation.
     */
    final User user;

    /**
     * Helper component for retrieve data.
     */
    final WebHelper webHelper;

    public GenerateHtmlForVariable(User user, WebHelper webHelper) {
        this.user = user;
        this.webHelper = webHelper;
    }

    @Override
    public GenerateHtmlForVariableResult onDate(DateFormat dateFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputDate", "width: 100px;");
        if (value instanceof Date) {
            result.setValue(CalendarUtil.formatDate((Date) value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onTime(TimeFormat timeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputTime", "width: 50px;");
        if (value instanceof Date) {
            result.setValue(CalendarUtil.formatTime((Date) value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onDateTime(DateTimeFormat dateTimeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputDateTime", "width: 150px;");
        if (value instanceof Date) {
            result.setValue(CalendarUtil.formatDateTime((Date) value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onExecutor(ExecutorFormat executorFormat, GenerateHtmlForVariableContext context) {
        return new GenerateHtmlForVariableResult(context, createExecutorSelect(user, context.variable.getDefinition().getName(), executorFormat,
                context.variable.getValue(), true), null);
    }

    @Override
    public GenerateHtmlForVariableResult onBoolean(BooleanFormat booleanFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "checkbox", "inputBoolean", null);
        if (value instanceof Boolean && (Boolean) value) {
            result.setChecked(true);
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
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
        return new GenerateHtmlForVariableResult(context, getFileComponent(webHelper, variableName, (IFileVariable) value, !context.readonly), null);
    }

    @Override
    public GenerateHtmlForVariableResult onHidden(HiddenFormat hiddenFormat, GenerateHtmlForVariableContext context) {
        return new GenerateHtmlForVariableResult(context, hiddenFormat.getInputHtml(user, webHelper, context.variable), null);
    }

    @Override
    public GenerateHtmlForVariableResult onList(ListFormat listFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        String scriptingVariableName = context.variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat componentFormat = FormatCommons.createComponent(context.variable, 0);
        WfVariable templateComponentVariable = ViewUtil.createListComponentVariable(context.variable, -1, componentFormat, null);
        StringBuffer supportJs = new StringBuffer();
        GenerateHtmlForVariableResult templateComponentResult = componentFormat.processBy(this, context.copyFor(templateComponentVariable));
        if (!context.readonly) {
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("VARIABLE", variableName);
            substitutions.put("UNIQUENAME", scriptingVariableName);
            substitutions.put("COMPONENT_JS_HANDLER", ViewUtil.getComponentJSFunction(templateComponentVariable));
            InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/ViewUtil.EditList.js", ViewUtil.class);
            supportJs.append(templateComponentResult.scriptContent).append(WebUtils.getFormComponentScript(javascriptStream, substitutions));
        }
        List<Object> list = TypeConversionUtil.convertTo(List.class, context.variable.getValue());
        if (list == null) {
            list = new ArrayList<Object>();
        }
        Div result = new Div();
        result.setID(scriptingVariableName);
        result.setClass(context.readonly ? "viewList" : "editList");
        if (!context.readonly) {
            WfVariable indexesVariable = ViewUtil.createListIndexesVariable(context.variable, list.size());
            result.addElement(ViewUtil.getHiddenInput(indexesVariable));
            Div templateElementDiv = createTemplateElement(context);
            templateElementDiv.addElement(templateComponentResult.htmlStructureContent.replace("[]", "{}"));
            templateElementDiv.addElement(createRemoveElement(context));
            result.addElement(templateElementDiv);
        }
        for (int row = 0; row < list.size(); row++) {
            Object value = list.get(row);
            WfVariable componentVariable = ViewUtil.createListComponentVariable(context.variable, row, componentFormat, value);
            GenerateHtmlForVariableResult componentGeneratedHtml = componentFormat.processBy(this, context.copyFor(componentVariable));
            Div rowElement = createCollectionRowElement(context, row, componentGeneratedHtml);
            supportJs.append(componentGeneratedHtml.scriptContent);
            if (!context.readonly) {
                rowElement.addElement(createRemoveElement(context));
                result.addElement(new Div().addElement(rowElement));
            } else {
                result.addElement(rowElement);
            }
        }
        if (!context.readonly) {
            result.addElement(createAddElement(context));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), supportJs.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onMap(MapFormat mapFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        String scriptingVariableName = context.variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat keyFormat = FormatCommons.createComponent(context.variable, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(context.variable, 1);
        WfVariable templateComponentVariableKey = ViewUtil.createMapKeyComponentVariable(context.variable, -1, null);
        WfVariable templateComponentVariableValue = ViewUtil.createMapValueComponentVariable(context.variable, -1, null);
        StringBuffer supportJs = new StringBuffer();
        if (!context.readonly) {
            GenerateJSFunctionsForVariable generateJsForComponent = new GenerateJSFunctionsForVariable();
            String keyJsHandler = keyFormat.processBy(generateJsForComponent, templateComponentVariableKey);
            String valueJsHandler = valueFormat.processBy(generateJsForComponent, templateComponentVariableValue);
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("VARIABLE", variableName);
            substitutions.put("UNIQUENAME", scriptingVariableName);
            substitutions.put("COMPONENT_JS_HANDLER", keyJsHandler + "\n" + valueJsHandler);
            InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/ViewUtil.EditList.js", ViewUtil.class);
            GenerateHtmlForVariableResult generatedKeyHtmlData = keyFormat.processBy(this, context.copyFor(templateComponentVariableKey));
            String keyComponentJs = generatedKeyHtmlData.scriptContent;
            GenerateHtmlForVariableResult generatedValueHtmlData = valueFormat.processBy(this, context.copyFor(templateComponentVariableValue));
            String valueComponentJs = generatedValueHtmlData.scriptContent;
            supportJs.append(keyComponentJs).append(valueComponentJs).append(WebUtils.getFormComponentScript(javascriptStream, substitutions));
        }
        Map<Object, Object> map = TypeConversionUtil.convertTo(Map.class, context.variable.getValue());
        if (map == null) {
            map = new HashMap<Object, Object>();
        }
        Div result = new Div();
        result.setID(scriptingVariableName);
        result.setClass(context.readonly ? "viewList" : "editList");
        if (!context.readonly) {
            WfVariable indexesVariable = ViewUtil.createListIndexesVariable(context.variable, map.size());
            result.addElement(ViewUtil.getHiddenInput(indexesVariable));

            Div templateElementDiv = createTemplateElement(context);
            GenerateHtmlForVariableResult componentGeneratedHtmlKey = keyFormat.processBy(this, context.copyFor(templateComponentVariableKey));
            GenerateHtmlForVariableResult componentGeneratedHtmlValue = valueFormat.processBy(this, context.copyFor(templateComponentVariableValue));
            templateElementDiv.addElement(componentGeneratedHtmlKey.htmlStructureContent.replace("[]", "{}"));
            templateElementDiv.addElement(componentGeneratedHtmlValue.htmlStructureContent.replace("[]", "{}"));
            templateElementDiv.addElement(createRemoveElement(context));
            result.addElement(templateElementDiv);

            int row = -1;
            for (Object key : map.keySet()) {
                row++;
                WfVariable keyComponentVariable = ViewUtil.createMapKeyComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult keyGeneratedHtml = keyFormat.processBy(this, context.copyFor(keyComponentVariable));
                WfVariable valueComponentVariable = ViewUtil.createMapValueComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult valueGeneratedHtml = valueFormat.processBy(this, context.copyFor(valueComponentVariable));

                String htmlStructureContent = keyGeneratedHtml.htmlStructureContent + valueGeneratedHtml.htmlStructureContent;
                String scriptContent = keyGeneratedHtml.scriptContent + valueGeneratedHtml.scriptContent;
                Div elementRow = createCollectionRowElement(context, row, new GenerateHtmlForVariableResult(context, htmlStructureContent,
                        scriptContent));
                elementRow.addElement(createRemoveElement(context));
                result.addElement(new Div().addElement(elementRow));

                supportJs.append(keyGeneratedHtml.scriptContent);
                supportJs.append(valueGeneratedHtml.scriptContent);
            }
            result.addElement(createAddElement(context));
        } else {
            Table table = new Table();
            table.setClass("list");
            result.addElement(table);
            int row = -1;
            for (Object key : map.keySet()) {
                row++;
                TR tr = new TR();
                table.addElement(tr);
                TD keyTd = new TD();
                keyTd.setClass("list");
                tr.addElement(keyTd);
                WfVariable keyComponentVariable = ViewUtil.createMapKeyComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult keyGeneratedHtml = keyFormat.processBy(this, context.copyFor(keyComponentVariable));
                keyTd.addElement(createCollectionRowElement(context, row, keyGeneratedHtml));
                TD valueTd = new TD();
                valueTd.setClass("list");
                tr.addElement(valueTd);
                WfVariable valueComponentVariable = ViewUtil.createMapValueComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult valueGeneratedHtml = valueFormat.processBy(this, context.copyFor(valueComponentVariable));
                valueTd.addElement(createCollectionRowElement(context, row, valueGeneratedHtml));

                supportJs.append(keyGeneratedHtml.scriptContent);
                supportJs.append(valueGeneratedHtml.scriptContent);
            }
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), supportJs.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onProcessId(ProcessIdFormat processIdFormat, GenerateHtmlForVariableContext context) {
        return generateNumberHtml(context);
    }

    @Override
    public GenerateHtmlForVariableResult onString(StringFormat stringFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputString", null);
        if (value != null) {
            String variableName = context.getVariableName();
            result.setValue(stringFormat.formatHtml(user, webHelper, context.processId, variableName, value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onTextString(TextFormat textFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        TextArea result = new TextArea().setName(variableName).setDisabled(context.readonly);
        result.setClass("inputText");
        if (value != null) {
            result.setTagText(textFormat.format(value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onFormattedTextString(FormattedTextFormat textFormat, GenerateHtmlForVariableContext context) {
        String variableName = context.variable.getDefinition().getName();
        Object value = context.variable.getValue();
        TextArea result = new TextArea().setName(variableName).setDisabled(context.readonly);
        result.setClass("inputFormattedText");
        if (value != null) {
            result.setTagText(textFormat.formatHtml(user, webHelper, context.processId, variableName, value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    @Override
    public GenerateHtmlForVariableResult onUserType(UserTypeFormat userTypeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        UserType userType = userTypeFormat.getUserType();
        UserTypeMap userTypeMap = (UserTypeMap) value;
        if (userTypeMap == null) {
            userTypeMap = new UserTypeMap(userType);
        }
        StringBuffer supportJs = new StringBuffer();
        Table result = new Table();
        result.setClass("list");
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            TR TR = new TR();
            result.addElement(TR);
            TD nameTd = new TD();
            nameTd.setClass("list");
            nameTd.setTagText(attributeDefinition.getName());
            TR.addElement(nameTd);
            TD valueTd = new TD();
            valueTd.setClass("list");
            Object attributeValue = userTypeMap.get(attributeDefinition.getName());
            WfVariable componentVariable = ViewUtil.createUserTypeComponentVariable(context.variable, attributeDefinition, attributeValue);
            GenerateHtmlForVariableResult attributeGeneratedHtml = componentVariable.getDefinition().getFormatNotNull()
                    .processBy(this, new GenerateHtmlForVariableContext(componentVariable, context.processId, context.readonly));
            valueTd.addElement(attributeGeneratedHtml.htmlStructureContent);
            TR.addElement(valueTd);
            supportJs.append(attributeGeneratedHtml.scriptContent);
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), supportJs.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onOther(VariableFormat variableFormat, GenerateHtmlForVariableContext context) {
        WfVariable variable = context.variable;
        if (!context.readonly) {
            if (variableFormat instanceof VariableInputSupport) {
                return new GenerateHtmlForVariableResult(context, ((VariableInputSupport) variableFormat).getInputHtml(user, webHelper, variable),
                        null);
            } else {
                throw new InternalApplicationException("No input method implemented for " + variableFormat);
            }
        } else {
            if (variableFormat instanceof VariableDisplaySupport) {
                VariableDisplaySupport displaySupport = (VariableDisplaySupport) variableFormat;
                return new GenerateHtmlForVariableResult(context, displaySupport.formatHtml(user, webHelper, context.processId, variable
                        .getDefinition().getName(), variable.getValue()), null);
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

    public static String createExecutorSelect(String variableName, List<? extends Executor> executors, Object value, boolean javaSort, boolean enabled) {
        StringBuilder html = new StringBuilder("<select name=\"").append(variableName).append("\"");
        if (!enabled) {
            html.append(" disabled=\"true\"");
        }
        html.append(">");
        if (javaSort) {
            Collections.sort(executors);
        }
        html.append("<option value=\"\"> ------------------------- </option>");
        for (Executor executor : executors) {
            html.append("<option value=\"ID").append(executor.getId()).append("\"");
            if (Objects.equal(executor, value)) {
                html.append(" selected");
            }
            html.append(">").append(executor.getLabel()).append("</option>");
        }
        html.append("</select>");
        return html.toString();
    }

    public static String getFileComponent(WebHelper webHelper, String variableName, IFileVariable value, boolean enabled) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" />";
        }
        Preconditions.checkNotNull(webHelper, "webHelper");
        String id = webHelper.getRequest().getParameter("id");
        UploadedFile file = FormSubmissionUtils.getUserInputFiles(webHelper.getRequest(), id).get(variableName);
        if (value != null && file == null) {
            // display file
            file = new UploadedFile(value);
            FormSubmissionUtils.addUserInputFile(webHelper.getRequest(), id, variableName, file);
        } else if (value == null && file != null) {
            // sf1095
            file = null;
            FormSubmissionUtils.removeUserInputFile(webHelper.getRequest(), id, variableName);
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

    /**
     * Generates result for input number.
     * 
     * @param context
     *            HTML generation context.
     * @return Returns data for generating form.
     */
    private GenerateHtmlForVariableResult generateNumberHtml(GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputNumber", null);
        if (value instanceof Number) {
            result.setValue(value.toString());
        }
        return new GenerateHtmlForVariableResult(context, result.toString(), null);
    }

    /**
     * Create HTML Input element ({@link Input}) for variable.
     * 
     * @param context
     *            HTML generation context.
     * @param type
     *            Input element type.
     * @param elementClass
     *            HTML element class.
     * @param style
     *            HTML element style.
     * @return Returns HTML Input element.
     */
    Input createInput(GenerateHtmlForVariableContext context, String type, String elementClass, String style) {
        String variableName = context.getVariableName();
        Input result = new Input().setType(type).setName(variableName).setDisabled(context.readonly);
        if (!Strings.isNullOrEmpty(style)) {
            result.setStyle(style);
        }
        if (!Strings.isNullOrEmpty(elementClass)) {
            result.setClass(elementClass);
        }
        return result;
    }

    /**
     * Generates HTML div element for template container.
     * 
     * @param context
     *            HTML generation context.
     * @return Returns HTML div element for template container.
     */
    private Div createTemplateElement(GenerateHtmlForVariableContext context) {
        Div result = new Div();
        result.addAttribute("template", true);
        result.addAttribute("name", context.getVariableName());
        result.setStyle("display:none");
        return result;
    }

    /**
     * Generates remove button for list and so on containers.
     * 
     * @param context
     *            HTML generation context.
     * @return Returns remove button.
     */
    private Input createRemoveElement(GenerateHtmlForVariableContext context) {
        Input removeButton = new Input();
        removeButton.setType("button");
        removeButton.setValue(" - ");
        removeButton.setStyle("width: 30px;");
        removeButton.setOnClick("remove" + context.getScriptingNameWithoutDots() + "(this);");
        return removeButton;
    }

    /**
     * Generates add button for list and so on containers.
     * 
     * @param context
     *            HTML generation context.
     * @return Returns add button.
     */
    private Div createAddElement(GenerateHtmlForVariableContext context) {
        Div result = new Div();
        Input addButton = new Input();
        result.addElement(addButton);
        addButton.setType("button");
        addButton.setID("btnAdd" + context.getScriptingNameWithoutDots());
        addButton.setStyle("width: 30px;");
        addButton.setValue(" + ");
        return result;
    }

    /**
     * Create HTML div element for collection row.
     * 
     * @param context
     *            HTML generation context.
     * @param row
     *            Row index
     * @param componentGeneratedHtml
     *            Generated HTML for displaying collection element.
     * @return
     */
    private Div createCollectionRowElement(GenerateHtmlForVariableContext context, int row, GenerateHtmlForVariableResult componentGeneratedHtml) {
        Div rowElement = new Div();
        rowElement.addAttribute("row", row);
        rowElement.addAttribute("current", "");
        rowElement.addAttribute("name", context.getVariableName());
        rowElement.addElement(componentGeneratedHtml.htmlStructureContent);
        return rowElement;
    }
}
