package ru.runa.wf.web.ftl.component;

public class DisplayUserTypeList extends AbstractListUserVariables {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() throws Exception {
        initFields();
        return ViewUtil.getUserTypeListTable(user, webHelper, variableProvider.getVariableNotNull(variableName), null, variableProvider
                .getProcessId(), new UserTableColumns(variableProvider.getVariableNotNull(variableName), sortField, displayFields,
                displayMode == DisplayMode.MULTI_DIMENTIONAL_TABLE));
    }
}
