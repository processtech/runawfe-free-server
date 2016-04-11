package ru.runa.wf.web.ftl.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.AjaxFormComponent;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

import com.google.common.collect.Lists;

/**
 * shared code with {@link InputVariable}.
 *
 * @author dofs
 * @since 4.0.5
 */
public class EditLinkedLists extends AjaxFormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest() {
        boolean allowToAddElements = getParameterAs(boolean.class, 0);
        boolean allowToChangeElements = getParameterAs(boolean.class, 1);
        boolean allowToDeleteElements = getParameterAs(boolean.class, 2);
        List<WfVariable> variables = Lists.newArrayList();
        List<VariableFormat> componentFormats = Lists.newArrayList();
        List<List<?>> lists = Lists.newArrayList();
        StringBuffer rowTemplate = new StringBuffer();
        StringBuffer jsHandlers = new StringBuffer();
        StringBuffer jsVariableNamesArray = new StringBuffer();
        String uniqueName = null;
        int i = 3;
        int rowsCount = 0;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            if (rowsCount == 0) {
                uniqueName = variable.getDefinition().getScriptingNameWithoutDots();
            }
            VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
            List<Object> list = TypeConversionUtil.convertTo(List.class, variable.getValue());
            if (list == null) {
                list = new ArrayList<Object>();
            }
            if (variables.size() != 0) {
                jsVariableNamesArray.append(", ");
            }
            jsVariableNamesArray.append("\"").append(variableName).append("\"");
            variables.add(variable);
            componentFormats.add(componentFormat);
            lists.add(list);
            String jsHandler = ViewUtil.getComponentJSFunction(variable);
            if (jsHandlers.indexOf(jsHandler, 0) == -1) {
                jsHandlers.append(jsHandler);
            }
            rowTemplate.append("<td>");
            WfVariable templateComponentVariable = ViewUtil.createListComponentVariable(variable, -1, componentFormat, null);
            String inputComponentHtml = getComponentInput(templateComponentVariable, true);
            inputComponentHtml = inputComponentHtml.replaceAll("\"", "'");
            rowTemplate.append(inputComponentHtml);
            rowTemplate.append("</td>");
            if (list.size() > rowsCount) {
                rowsCount = list.size();
            }
            i++;
        }
        if (variables.size() > 0) {
            StringBuffer html = new StringBuffer();
            Map<String, String> substitutions = new HashMap<String, String>();
            substitutions.put("ROW_TEMPLATE", rowTemplate.toString());
            substitutions.put("JS_HANDLERS", jsHandlers.toString());
            substitutions.put("VARIABLE_NAMES", jsVariableNamesArray.toString());
            substitutions.put("UNIQUENAME", uniqueName);
            html.append(exportScript(substitutions, false));
            html.append("<table id=\"ell").append(uniqueName).append("\" class=\"editLinkedLists\" rowsCount=\"").append(rowsCount).append("\">");
            String operationsColumn = null;
            if (allowToAddElements || allowToDeleteElements) {
                operationsColumn = "<th style=\"width: 30px;\">";
                if (allowToAddElements) {
                    operationsColumn += "<input type=\"button\" id=\"ell" + uniqueName + "ButtonAdd\" value=\" + \" />";
                }
                operationsColumn += "</th>";
            }
            html.append(ViewUtil.generateTableHeader(variables, variableProvider, operationsColumn));
            for (WfVariable containerVariable : variables) {
                WfVariable indexesVariable = ViewUtil.createListIndexesVariable(containerVariable, rowsCount);
                html.append(ViewUtil.getHiddenInput(indexesVariable));
            }
            for (int row = 0; row < rowsCount; row++) {
                renderRow(html, uniqueName, variables, lists, componentFormats, row, allowToChangeElements, allowToDeleteElements);
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    private void renderRow(StringBuffer html, String uniqueName, List<WfVariable> variables, List<List<?>> lists,
            List<VariableFormat> componentFormats, int row, boolean allowToChangeElements, boolean allowToDeleteElements) {
        html.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variables.size(); column++) {
            Object o = lists.get(column).size() > row ? lists.get(column).get(row) : null;
            VariableFormat componentFormat = componentFormats.get(column);
            renderColumn(html, variables.get(column), componentFormat, o, row, column, allowToChangeElements);
        }
        if (allowToDeleteElements) {
            html.append("<td><input type='button' value=' - ' onclick=\"ell").append(uniqueName).append("RemoveRow(this);\" /></td>");
        }
        html.append("</tr>");
    }

    private void renderColumn(StringBuffer html, WfVariable containerVariable, VariableFormat componentFormat, Object value, int row, int column,
            boolean enabled) {
        WfVariable componentVariable = ViewUtil.createListComponentVariable(containerVariable, row, componentFormat, value);
        html.append("<td column=\"").append(column).append("\">");
        html.append(getComponentInput(componentVariable, enabled));
        html.append("</td>");
    }

    private String getComponentInput(WfVariable componentVariable, boolean enabled) {
        if (enabled) {
            return ViewUtil.getComponentInput(user, webHelper, componentVariable);
        }
        String html = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
        html += ViewUtil.getHiddenInput(componentVariable);
        return html;
    }

}
