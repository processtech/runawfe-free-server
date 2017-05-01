package ru.runa.wf.web.ftl.component.legacy;

import ru.runa.wf.web.ftl.component.ViewUtil;


public class DisplayListUserVariables extends AbstractListUserVariables {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        initFields();
        return ViewUtil.getUserTypeListTable(user, webHelper, variableProvider.getVariableNotNull(variableName), null, variableProvider.getProcessId(),
                sortField, displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE);
    }

}
