package ru.runa.wf.web.ftl.component;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.SelectableOption;

@SuppressWarnings("unchecked")
public class DisplayMultipleSelectInList extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        List<Object> list = getParameterVariableValue(List.class, 1, null);
        if (list == null) {
            list = Lists.newArrayList();
        }
        List<Object> selectedValues = variableProvider.getValue(List.class, variableName);
        StringBuffer html = new StringBuffer();
        html.append("<div class=\"multipleSelectFromList\">");
        for (Object option : list) {
            String optionValue;
            String optionLabel;
            if (option instanceof SelectableOption) {
                SelectableOption selectable = (SelectableOption) option;
                optionValue = selectable.getValue();
                optionLabel = selectable.getLabel();
            } else if (option instanceof Executor) {
                Executor executor = (Executor) option;
                optionValue = "ID" + executor.getId();
                optionLabel = executor.getLabel();
            } else {
                optionValue = String.valueOf(option);
                optionLabel = optionValue;
            }
            String id = variableName + "_" + optionValue;
            html.append("<input id=\"").append(id).append("\"");
            html.append(" type=\"checkbox\" value=\"").append(optionValue).append("\"");
            html.append(" name=\"").append(variableName).append("\"");
            if (selectedValues != null && selectedValues.contains(option)) {
                html.append(" checked=\"true\"");
            }
            html.append("style=\"width: 30px;\" disabled=\"true\">");
            html.append("<label for=\"").append(id).append("\">");
            html.append(optionLabel);
            html.append("</label><br>");
        }
        html.append("</div>");
        return html;
    }

}
