package ru.runa.wfe.script.permission;

import java.util.Set;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.SecuredObject;

@XmlType(name = RemoveAllPermissionsFromProcessesOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveAllPermissionsFromProcessesOperation extends RemoveAllPermissionsFromSecuredObjectsOperation {

    public static final String SCRIPT_NAME = "removeAllPermissionsFromProcesses";

    public RemoveAllPermissionsFromProcessesOperation() {
        super(null);
    }

    @Override
    protected Set<SecuredObject> getSecuredObjects(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getProcesses(context, identityNames);
    }

}
