package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = AddPermissionsOnGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnGroupOperation extends ChangePermissionsOnIdentifiablesOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnGroup";

    public AddPermissionsOnGroupOperation() {
        super(SecuredObjectType.GROUP, NamedIdentityType.EXECUTOR, ChangePermissionType.ADD);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getGroups(context, identityNames);
    }

}
