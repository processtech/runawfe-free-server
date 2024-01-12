package ru.runa.wf.web.ftl.component;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

@Deprecated
public class DisplayLinkedLists extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        List<WfVariable> variables = Lists.newArrayList();
        List<VariableFormat> componentFormats = Lists.newArrayList();
        List<List<?>> lists = Lists.newArrayList();
        String firstParameter = getParameterAsString(0);
        boolean componentView = !"false".equals(firstParameter);
        int i = ("false".equals(firstParameter) || "true".equals(firstParameter)) ? 1 : 0;
        int rowsCount = 0;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
            List<Object> list = TypeConversionUtil.convertTo(List.class, variable.getValue());
            if (list == null) {
                list = new ArrayList<Object>();
            }
            variables.add(variable);
            componentFormats.add(componentFormat);
            lists.add(list);
            if (list.size() > rowsCount) {
                rowsCount = list.size();
            }
            i++;
        }
        if (variables.size() > 0) {
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"displayLinkedLists\">");
            html.append(ViewUtil.generateTableHeader(variables, variableProvider, null));
            for (int row = 0; row < rowsCount; row++) {
                renderRow(html, variables, lists, componentFormats, componentView, row);
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

    private void renderRow(StringBuffer html, List<WfVariable> variables, List<List<?>> lists, List<VariableFormat> componentFormats,
            boolean componentView, int row) {
        html.append("<tr row=\"").append(row).append("\">");
        for (int column = 0; column < variables.size(); column++) {
            Object o = (lists.get(column).size() > row) ? lists.get(column).get(row) : null;
            VariableFormat componentFormat = componentFormats.get(column);
            renderColumn(html, variables.get(column), componentFormat, o, componentView, row, column);
        }
        html.append("</tr>");
    }

    private void renderColumn(StringBuffer html, WfVariable containerVariable, VariableFormat componentFormat, Object value, boolean componentView,
            int row, int column) {
        WfVariable componentVariable = ViewUtil.createListComponentVariable(containerVariable, row, componentFormat, value);
        html.append("<td column=\"").append(column).append("\">");
        html.append(getComponentOutput(componentVariable, componentView));
        html.append("</td>");
    }

    private String getComponentOutput(WfVariable componentVariable, boolean componentView) {
        if (componentView) {
            return ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
        } else {
            return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
        }
    }

}
