package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = SetPermissionsOnRelationGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOnRelationGroupOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "setPermissionsOnRelationGroup";

    public SetPermissionsOnRelationGroupOperation() {
        super(RelationsGroupSecure.INSTANCE, ChangePermissionType.SET);
    }
}
