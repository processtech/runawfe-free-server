package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = AddPermissionsOnRelationGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnRelationGroupOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnRelationGroup";

    public AddPermissionsOnRelationGroupOperation() {
        super(RelationsGroupSecure.INSTANCE, ChangePermissionType.ADD);
    }
}
