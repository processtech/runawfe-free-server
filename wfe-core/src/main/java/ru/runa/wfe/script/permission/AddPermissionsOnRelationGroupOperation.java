package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = AddPermissionsOnRelationGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnRelationGroupOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnRelationGroup";

    public AddPermissionsOnRelationGroupOperation() {
        super(SecuredSingleton.RELATIONS, ChangePermissionType.ADD);
    }
}
