package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = RemovePermissionsOnProcessesOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnProcessesOperation extends ChangePermissionsOnIdentifiablesOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnProcesses";

    public RemovePermissionsOnProcessesOperation() {
        super(SecuredObjectType.PROCESS, null, ChangePermissionType.REMOVE);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getProcesses(context, identityNames);
    }

}
