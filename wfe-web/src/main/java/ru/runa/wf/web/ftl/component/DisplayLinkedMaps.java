package ru.runa.wf.web.ftl.component;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DisplayLinkedMaps extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        List<WfVariable> variables = Lists.newArrayList();
        String firstParameter = getParameterAsString(0);
        boolean componentView = "true".equals(firstParameter);
        int i = ("false".equals(firstParameter) || "true".equals(firstParameter)) ? 1 : 0;
        String keyFormatClassName = null;
        while (true) {
            String variableName = getParameterAsString(i);
            if (variableName == null) {
                break;
            }
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            if (variable.getValue() == null) {
                variable.setValue(Maps.newHashMap());
            }
            Preconditions.checkArgument(variable.getValue() instanceof Map, variable);
            if (keyFormatClassName != null) {
                Preconditions.checkArgument(Objects.equal(variable.getDefinition().getFormatComponentClassNames()[0], keyFormatClassName),
                        "Maps should be linked by keys with equal format");
            } else {
                keyFormatClassName = variable.getDefinition().getFormatComponentClassNames()[0];
            }
            variables.add(variable);
            i++;
        }
        if (variables.size() > 0) {
            StringBuffer html = new StringBuffer();
            html.append("<table class=\"displayLinkedMaps\">");
            html.append(ViewUtil.generateTableHeader(variables, variableProvider, null));
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) variables.get(0).getValue()).entrySet()) {
                html.append("<tr>");
                for (int column = 0; column < variables.size(); column++) {
                    WfVariable containerVariable = variables.get(column);
                    WfVariable componentVariable = ViewUtil.createMapComponentVariable(containerVariable, entry.getKey());
                    String value;
                    if (componentView) {
                        value = ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
                    } else {
                        value = ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
                    }
                    html.append("<td>").append(value).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
            return html.toString();
        }
        return "-";
    }

}
