package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

@XmlType(name = SetPermissionsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOperation extends ChangePermissionsOperation {

    public static final String SCRIPT_NAME = "setPermissions";

    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().setPermissions(xmlExecutor, objectNames, permissions);
    }
}
