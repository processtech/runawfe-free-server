package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;

@XmlType(name = RemoveAllPermissionsFromExecutorOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveAllPermissionsFromExecutorOperation extends RemoveAllPermissionsFromIdentifiablesOperation {

    public static final String SCRIPT_NAME = "removeAllPermissionsFromExecutor";

    public RemoveAllPermissionsFromExecutorOperation() {
        super(NamedIdentityType.EXECUTOR);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getExecutors(context, identityNames);
    }

}
