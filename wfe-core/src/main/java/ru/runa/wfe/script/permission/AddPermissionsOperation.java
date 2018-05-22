package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

@XmlType(name = AddPermissionsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOperation extends ChangePermissionsOperation {

    public static final String SCRIPT_NAME = "addPermissions";

    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().addPermissions(context.getUser(), xmlExecutor, objectNames, permissions);
    }
}
