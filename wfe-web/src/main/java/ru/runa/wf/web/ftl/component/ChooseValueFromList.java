package ru.runa.wf.web.ftl.component;

import java.util.List;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class ChooseValueFromList extends FormComponent {
    private static final long serialVersionUID = 1L;

    protected List<String> getOptions() {
        List<String> list = Lists.newArrayList();
        int i = 1;
        while (true) {
            String option = getParameterAsString(i);
            if (option == null) {
                break;
            }
            list.add(option);
            i++;
        }
        return list;
    }

    @Override
    protected Object renderRequest() throws Exception {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        String scriptingName = variable.getDefinition().getScriptingNameWithoutDots();
        String html = "";
        html += "<div class='radioset'>";
        int i = 1;
        for (String option : getOptions()) {
            html += "<input type='radio' name='" + variableName + "' value='" + option + "' id='" + scriptingName + "." + i + "' ";
            if (Objects.equal(variable.getValue(), option)) {
                html += "checked='checked' ";
            }
            html += "/><label for='" + scriptingName + "." + i + "'>" + option + "</label>";
            i++;
        }
        html += "</div>";
        return html;
    }

}
