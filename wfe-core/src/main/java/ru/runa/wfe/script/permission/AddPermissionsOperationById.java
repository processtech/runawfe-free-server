package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;

/**
 * 
 * @author mezubarev 
 */
@XmlType(name = AddPermissionsOperationById.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOperationById extends ChangePermissionsOperationById {

    public static final String SCRIPT_NAME = "addPermissionsById";
    
    @Override
    public void execute(ScriptExecutionContext context) {
        context.getAuthorizationLogic().addPermissionsById(context.getUser(), xmlExecutor, permissions, xmlType, ids);
    }
}
