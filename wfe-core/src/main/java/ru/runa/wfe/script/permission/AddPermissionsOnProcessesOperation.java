package ru.runa.wfe.script.permission;

import java.util.Set;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = AddPermissionsOnProcessesOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnProcessesOperation extends ChangePermissionsOnSecuredObjectsOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnProcesses";

    public AddPermissionsOnProcessesOperation() {
        super(SecuredObjectType.PROCESS, null, ChangePermissionType.ADD);
    }

    @Override
    protected Set<SecuredObject> getSecuredObjects(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getProcesses(context, identityNames);
    }

}
