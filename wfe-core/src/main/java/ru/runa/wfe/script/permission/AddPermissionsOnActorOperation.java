package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = AddPermissionsOnActorOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnActorOperation extends ChangePermissionsOnIdentifiablesOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnActor";

    public AddPermissionsOnActorOperation() {
        super(SecuredObjectType.ACTOR, NamedIdentityType.EXECUTOR, ChangePermissionType.ADD);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getActors(context, identityNames);
    }

}
