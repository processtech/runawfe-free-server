package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = AddPermissionsOnAllGroupsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnAllGroupsOperation extends ChangePermissionsOnAllOperations {
    public static final String SCRIPT_NAME = "addPermissionsOnAllGroups";

    public AddPermissionsOnAllGroupsOperation() {
        super(SecuredObjectType.GROUP, NamedIdentityType.EXECUTOR, ChangePermissionType.ADD);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context) {
        return IdentifiebleSetConvertions.getGroups(context);
    }
}
