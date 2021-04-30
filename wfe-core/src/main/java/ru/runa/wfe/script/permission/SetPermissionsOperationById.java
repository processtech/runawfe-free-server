package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

/**
 * 
 * @author mezubarev 
 */
@XmlType(name = SetPermissionsOperationById.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOperationById extends ChangePermissionsOperationById {

    public static final String SCRIPT_NAME = "setPermissionsById";
    
    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().setPermissionsById(context.getUser(), xmlExecutor, permissions, xmlType, ids);
    }
}
