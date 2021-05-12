package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptValidationException;

@XmlType(name = RemoveAllPermissionsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveAllPermissionsOperation extends ChangePermissionsOperation {

    public static final String SCRIPT_NAME = "removeAllPermissions";

    @Override
    public void validate(ScriptExecutionContext context) {
        if (permissions != null && !permissions.isEmpty()) {
            throw new ScriptValidationException("'permission' children are not allowed");
        }
        super.validate(context);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().removeAllPermissions(context.getUser(), xmlExecutor, objectNames);
    }
}
