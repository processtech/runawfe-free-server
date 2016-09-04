package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = RemovePermissionsOnRelationGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnRelationGroupOperation extends ChangePermissionsOnIdentifiableOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnRelationGroup";

    public RemovePermissionsOnRelationGroupOperation() {
        super(RelationsGroupSecure.INSTANCE, ChangePermissionType.REMOVE);
    }
}
