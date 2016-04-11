package ru.runa.wf.web.ftl.component;

import java.util.List;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.extension.handler.var.ListIndexUtils;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

public class DisplayListElement extends FormComponent {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        List<Object> list = (List<Object>) variable.getValue();
        int index = getRichComboParameterAs(int.class, 1);
        index = ListIndexUtils.adjustIndex(index);
        Object object = null;
        if (index < list.size()) {
            object = list.get(index);
        }
        VariableFormat componentFormat = FormatCommons.createComponent(variable, 0);
        WfVariable componentVariable = ViewUtil.createListComponentVariable(variable, index, componentFormat, object);
        return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
    }

}
