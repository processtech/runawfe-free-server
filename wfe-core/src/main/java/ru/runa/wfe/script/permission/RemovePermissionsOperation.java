package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

@XmlType(name = RemovePermissionsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOperation extends ChangePermissionsOperation {

    public static final String SCRIPT_NAME = "removePermissions";

    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().removePermissions(context.getUser(), xmlExecutor, objectNames, permissions);
    }
}
