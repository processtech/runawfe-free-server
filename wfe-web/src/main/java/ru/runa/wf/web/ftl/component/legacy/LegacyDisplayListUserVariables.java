package ru.runa.wf.web.ftl.component.legacy;

public class LegacyDisplayListUserVariables extends LegacyAbstractListUserVariables {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        initFields();
        return getUserTypeListTable(user, webHelper, variableProvider.getVariableNotNull(variableName), null,
                sortField, displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE);
    }

}
