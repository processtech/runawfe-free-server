package ru.runa.wf.web.ftl.component;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.var.SelectableOption;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public abstract class AbstractCheckboxSelectFromVariableList extends FormComponent implements FormComponentSubmissionHandler {
    private static final long serialVersionUID = 1L;

    protected abstract boolean isMultiple();

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        List<Object> selectedValues = variableProvider.getValue(List.class, variableName);
        String listVariableName = getParameterAsString(1);
        boolean horizontalLayout = "horizontal".equals(getParameterAsString(2));
        WfVariable listVariable = variableProvider.getVariableNotNull(listVariableName);
        VariableFormat componentFormat = FormatCommons.createComponent(listVariable, 0);
        List<Object> list = (List<Object>) listVariable.getValue();
        if (list == null) {
            list = Lists.newArrayList();
        }
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"selectFromList\">");
        for (int i = 0; i < list.size(); i++) {
            Object option = list.get(i);
            String value = String.valueOf(i);
            String label;
            if (option instanceof SelectableOption) {
                label = ((SelectableOption) option).getLabel();
            } else {
                WfVariable variable = ViewUtil.createListComponentVariable(listVariable, i, componentFormat, option);
                label = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), variable);
            }
            String id = variableName + "_" + value;
            html.append("<input id=\"").append(id).append("\"");
            html.append(" type=\"").append(isMultiple() ? "checkbox" : "radio");
            html.append("\" value=\"").append(value).append("\"");
            html.append(" name=\"").append(variableName).append("\"");
            if (selectedValues != null && selectedValues.contains(option)) {
                html.append(" checked=\"true\"");
            }
            html.append(" style=\"width: 30px;\">");
            html.append("<label for=\"").append(id).append("\">");
            html.append(label);
            html.append("</label>");
            if (!horizontalLayout) {
                html.append("<br>");
            }
        }
        html.append("</div>");
        return html;
    }

    @Override
    public Map<String, ? extends Object> extractVariables(Interaction interaction, VariableDefinition variableDefinition,
            Map<String, ? extends Object> userInput, Map<String, String> errors) throws Exception {
        Map<String, Object> result = Maps.newHashMap();
        String[] indexes = (String[]) userInput.get(getVariableNameForSubmissionProcessing());
        if (indexes == null || indexes.length == 0) {
            return result;
        }
        List<?> list = getParameterVariableValue(List.class, 1, null);
        List<Object> selected = Lists.newArrayListWithExpectedSize(indexes.length);
        for (String index : indexes) {
            selected.add(list.get(TypeConversionUtil.convertTo(int.class, index)));
        }
        result.put(getVariableNameForSubmissionProcessing(), isMultiple() ? selected : selected.get(0));
        return result;
    }

}
