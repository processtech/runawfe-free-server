package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

public class DisplayMapElement extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        Object key = getRichComboParameterAs(Object.class, 1);
        WfVariable componentVariable = ViewUtil.createMapComponentVariable(variable, key);
        return ViewUtil.getOutput(user, webHelper, variableProvider.getProcessId(), componentVariable);
    }

}
