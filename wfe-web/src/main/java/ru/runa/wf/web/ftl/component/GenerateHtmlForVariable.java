package ru.runa.wf.web.ftl.component;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import ru.runa.common.WebResources;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.servlet.AjaxExecutorsList;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
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
        return new GenerateHtmlForVariableResult(context, result.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onTime(TimeFormat timeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputTime", "width: 50px;");
        if (value instanceof Date) {
            result.setValue(CalendarUtil.formatTime((Date) value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onDateTime(DateTimeFormat dateTimeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "text", "inputDateTime", "width: 150px;");
        if (value instanceof Date) {
            result.setValue(CalendarUtil.formatDateTime((Date) value));
        }
        return new GenerateHtmlForVariableResult(context, result.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onExecutor(ExecutorFormat executorFormat, GenerateHtmlForVariableContext context) {
        if (context.readonly) {
            Input result = createInput(context, "text", "inputString", null);
            if (context.variable.getValue() instanceof Executor) {
                result.setValue(((Executor) context.variable.getValue()).getLabel());
            }
            return new GenerateHtmlForVariableResult(context, result.toString());
        }
        AjaxExecutorsList.Type type;
        if (executorFormat instanceof ActorFormat) {
            type = AjaxExecutorsList.Type.actor;
        } else if (executorFormat instanceof GroupFormat) {
            type = AjaxExecutorsList.Type.group;
        } else {
            type = AjaxExecutorsList.Type.executor;
        }
        return new GenerateHtmlForVariableResult(context, createExecutorAutoSelect(context.variable.getDefinition().getName(), type,
                context.variable.getValue() instanceof TemporaryGroup, (Executor) context.variable.getValue()));
    }

    @Override
    public GenerateHtmlForVariableResult onBoolean(BooleanFormat booleanFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        Input result = createInput(context, "checkbox", "inputBoolean", null);
        if (value instanceof Boolean && (Boolean) value) {
            result.setChecked(true);
        }
        return new GenerateHtmlForVariableResult(context, result.toString());
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
        return new GenerateHtmlForVariableResult(context, getFileComponent(webHelper, variableName, (FileVariable) value, !context.readonly));
    }

    @Override
    public GenerateHtmlForVariableResult onHidden(HiddenFormat hiddenFormat, GenerateHtmlForVariableContext context) {
        return new GenerateHtmlForVariableResult(context, hiddenFormat.getInputHtml(user, webHelper, context.variable));
    }

    @Override
    public GenerateHtmlForVariableResult onList(ListFormat listFormat, GenerateHtmlForVariableContext context) {
        String scriptingVariableName = context.variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat componentFormat = FormatCommons.createComponent(context.variable, 0);
        WfVariable templateComponentVariable = ViewUtil.createListComponentVariable(context.variable, -1, componentFormat, null);
        GenerateHtmlForVariableResult templateComponentResult = componentFormat.processBy(this, context.copyFor(templateComponentVariable));
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
            templateElementDiv.addElement(templateComponentResult.content.replace("[]", "{}"));
            templateElementDiv.addElement(createRemoveElement(context));
            result.addElement(templateElementDiv);
        }
        for (int row = 0; row < list.size(); row++) {
            Object value = list.get(row);
            WfVariable componentVariable = ViewUtil.createListComponentVariable(context.variable, row, componentFormat, value);
            GenerateHtmlForVariableResult componentGeneratedHtml = componentFormat.processBy(this, context.copyFor(componentVariable));
            Div rowElement = createCollectionRowElement(context, row, componentGeneratedHtml);
            if (!context.readonly) {
                rowElement.addElement(createRemoveElement(context));
                result.addElement(new Div().addElement(rowElement));
            } else {
                result.addElement(rowElement);
            }
        }
        if (!context.readonly) {
            result.addElement(createAddElement(scriptingVariableName));
        }
        return new GenerateHtmlForVariableResult(context, result.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onMap(MapFormat mapFormat, GenerateHtmlForVariableContext context) {
        String scriptingVariableName = context.variable.getDefinition().getScriptingNameWithoutDots();
        VariableFormat keyFormat = FormatCommons.createComponent(context.variable, 0);
        VariableFormat valueFormat = FormatCommons.createComponent(context.variable, 1);
        WfVariable templateComponentVariableKey = ViewUtil.createMapKeyComponentVariable(context.variable, -1, null);
        WfVariable templateComponentVariableValue = ViewUtil.createMapValueComponentVariable(context.variable, -1, null);
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
            templateElementDiv.addElement(componentGeneratedHtmlKey.content.replace("[]", "{}"));
            templateElementDiv.addElement(componentGeneratedHtmlValue.content.replace("[]", "{}"));
            templateElementDiv.addElement(createRemoveElement(context));
            result.addElement(templateElementDiv);

            int row = -1;
            for (Object key : map.keySet()) {
                row++;
                WfVariable keyComponentVariable = ViewUtil.createMapKeyComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult keyGeneratedHtml = keyFormat.processBy(this, context.copyFor(keyComponentVariable));
                WfVariable valueComponentVariable = ViewUtil.createMapValueComponentVariable(context.variable, row, key);
                GenerateHtmlForVariableResult valueGeneratedHtml = valueFormat.processBy(this, context.copyFor(valueComponentVariable));

                String htmlStructureContent = keyGeneratedHtml.content + valueGeneratedHtml.content;
                Div elementRow = createCollectionRowElement(context, row, new GenerateHtmlForVariableResult(context, htmlStructureContent));
                elementRow.addElement(createRemoveElement(context));
                result.addElement(new Div().addElement(elementRow));
            }
            result.addElement(createAddElement(scriptingVariableName));
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
            }
        }
        return new GenerateHtmlForVariableResult(context, result.toString());
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
        return new GenerateHtmlForVariableResult(context, result.toString());
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
        return new GenerateHtmlForVariableResult(context, result.toString());
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
        return new GenerateHtmlForVariableResult(context, result.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onUserType(UserTypeFormat userTypeFormat, GenerateHtmlForVariableContext context) {
        Object value = context.variable.getValue();
        UserType userType = userTypeFormat.getUserType();
        UserTypeMap userTypeMap = (UserTypeMap) value;
        if (userTypeMap == null) {
            userTypeMap = new UserTypeMap(userType);
        }
        Table result = new Table();
        result.setClass("list");
        for (VariableDefinition attributeDefinition : userType.getAttributes()) {
            if (context.isChatView && !attributeDefinition.isEditableInChat()) {
                continue;
            }
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
                    .processBy(this, new GenerateHtmlForVariableContext(componentVariable, context.processId, context.readonly, context.isChatView));
            valueTd.addElement(attributeGeneratedHtml.content);
            TR.addElement(valueTd);
        }
        return new GenerateHtmlForVariableResult(context, result.toString());
    }

    @Override
    public GenerateHtmlForVariableResult onOther(VariableFormat variableFormat, GenerateHtmlForVariableContext context) {
        WfVariable variable = context.variable;
        if (!context.readonly) {
            if (variableFormat instanceof VariableInputSupport) {
                return new GenerateHtmlForVariableResult(context, ((VariableInputSupport) variableFormat).getInputHtml(user, webHelper, variable));
            } else {
                throw new InternalApplicationException("No input method implemented for " + variableFormat);
            }
        } else {
            if (variableFormat instanceof VariableDisplaySupport) {
                VariableDisplaySupport displaySupport = (VariableDisplaySupport) variableFormat;
                return new GenerateHtmlForVariableResult(context, displaySupport.formatHtml(user, webHelper, context.processId, variable
                        .getDefinition().getName(), variable.getValue()));
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
            if (Objects.equals(executor, value) || value instanceof String && value.equals(executor.getName())) {
                html.append(" selected");
            }
            html.append(">").append(executor.getLabel()).append("</option>");
        }
        html.append("</select>");
        return html.toString();
    }

    public static String createExecutorAutoSelect(String variableName, AjaxExecutorsList.Type type, boolean includingTemporaryGroups, Executor value) {
        StringBuilder html = new StringBuilder();
        html.append("<input type=\"hidden\" name=\"").append(variableName).append("\"");
        if (value != null) {
            html.append(" value=\"").append(value.getName()).append("\"");
        }
        html.append("/>");
        html.append("<input class=\"js-select-executor\" autocomplete=\"off\" type=\"text\"");
        html.append(" js-label-for=\"").append(variableName).append("\"");
        html.append(" js-executor-type=\"").append(type.name()).append("\"");
        html.append(" js-including-temporary-groups=\"").append(includingTemporaryGroups).append("\"");
        if (value != null) {
            html.append(" value=\"").append(value.getLabel()).append("\"");
        }
        html.append("/>");
        return html.toString();

    }

    public static String getFileComponent(WebHelper webHelper, String variableName, FileVariable value, boolean enabled) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "<input type=\"file\" name=\"" + variableName + "\" class=\"inputFile\" />";
        }
        Preconditions.checkNotNull(webHelper, "webHelper");
        String id = webHelper.getRequest().getParameter("id");
        UploadedFile file = FormSubmissionUtils.getUserInputFiles(webHelper.getRequest(), id).get(variableName);
        if (value != null && value.getName() != null && file == null) {
            // display file
            file = new UploadedFile(value);
            FormSubmissionUtils.addUserInputFile(webHelper.getRequest(), id, variableName, file);
        } else if ((value == null || value.getName() == null) && file != null) {
            // sf1095
            file = null;
            FormSubmissionUtils.removeUserInputFile(webHelper.getRequest(), id, variableName);
        }
        String attachImageUrl = webHelper.getUrl(Resources.IMAGE_ATTACH);
        String loadingImageUrl = webHelper.getUrl(Resources.IMAGE_LOADING);
        String deleteImageUrl = webHelper.getUrl(Resources.IMAGE_DELETE);
        String uploadFileTitle = webHelper.getMessage("message.upload.file");
        String loadingMessage = webHelper.getMessage("message.loading");
        String hideStyle = "style=\"display: none;\"";
        String html = "<div class=\"inputFileContainer\"" + (!enabled && file == null ? hideStyle : "") + ">";
        html += "<div class=\"dropzone\" " + (file != null ? hideStyle : "") + ">";
        html += "<label class=\"inputFileAttach\">";
        html += "<div class=\"inputFileAttachButtonDiv\"><img src=\"" + attachImageUrl + "\" />" + uploadFileTitle + "</div>";
        html += "<input class=\"inputFile inputFileAjax\" name=\"" + variableName + "\" type=\"file\">";
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
        if (file != null) {
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
        return new GenerateHtmlForVariableResult(context, result.toString());
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
        removeButton.setName("remove_" + context.variable.getDefinition().getScriptingNameWithoutDots());
        removeButton.setType("button");
        removeButton.setValue(" - ");
        removeButton.setStyle("width: 30px;");
        removeButton.setClass("remove");
        return removeButton;
    }

    /**
     * @return add button for list and so on containers.
     */
    private Div createAddElement(String scriptingVariableName) {
        Div result = new Div();
        Input addButton = new Input();
        result.addElement(addButton);
        addButton.setName("add_" + scriptingVariableName);
        addButton.setType("button");
        addButton.setClass("add");
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
        rowElement.addAttribute("name", context.getVariableName());
        rowElement.addElement(componentGeneratedHtml.content);
        return rowElement;
    }
}
