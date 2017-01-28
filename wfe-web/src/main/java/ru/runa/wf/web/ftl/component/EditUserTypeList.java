package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;

public class EditUserTypeList extends FormComponent  {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        final String variableName = getParameterAsString(0);
        return ViewUtil.getComponentInput(user, webHelper, variableProvider.getVariableNotNull(variableName));
    }
}