package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = RemovePermissionsOnGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnGroupOperation extends ChangePermissionsOnIdentifiablesOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnGroup";

    public RemovePermissionsOnGroupOperation() {
        super(SecuredObjectType.GROUP, NamedIdentityType.EXECUTOR, ChangePermissionType.REMOVE);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getGroups(context, identityNames);
    }

}
