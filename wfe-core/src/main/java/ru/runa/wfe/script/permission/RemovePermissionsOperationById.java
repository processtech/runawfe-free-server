package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

/**
 * 
 * @author mezubarev 
 */
@XmlType(name = RemovePermissionsOperationById.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOperationById extends ChangePermissionsOperationById {

    public static final String SCRIPT_NAME = "removePermissionsById";
    
    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().removePermissionsById(context.getUser(), xmlExecutor, permissions, xmlType, ids);
    }
}
