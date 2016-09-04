package ru.runa.wfe.script.permission;

import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.IdentifiebleSetConvertions;
import ru.runa.wfe.script.common.NamedIdentitySet.NamedIdentityType;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

@XmlType(name = RemovePermissionsOnRelationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnRelationOperation extends ChangePermissionsOnIdentifiablesOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnRelation";

    public RemovePermissionsOnRelationOperation() {
        super(SecuredObjectType.RELATION, NamedIdentityType.RELATION, ChangePermissionType.REMOVE);
    }

    @Override
    protected Set<Identifiable> getIdentifiables(ScriptExecutionContext context, Set<String> identityNames) {
        return IdentifiebleSetConvertions.getRelations(context, identityNames);
    }

}
