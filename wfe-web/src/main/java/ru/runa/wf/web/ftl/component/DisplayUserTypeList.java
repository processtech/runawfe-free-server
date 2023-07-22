package ru.runa.wf.web.ftl.component;

import java.util.List;

import ru.runa.wfe.var.dto.WfVariable;

public class DisplayUserTypeList extends AbstractUserTypeList {
    private static final long serialVersionUID = 1L;

    @Override
    protected UserTypeListModel parseParameters() {
        String variableName = getParameterAsString(0);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        boolean componentView = getParameterAs(boolean.class, 1);
        List<String> displayFields = getMultipleParameter(2);
        return new UserTypeListModel(variable, displayFields, componentView);
    }
}
