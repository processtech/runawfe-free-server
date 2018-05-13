package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = SetPermissionsOnRelationGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOnRelationGroupOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "setPermissionsOnRelationGroup";

    public SetPermissionsOnRelationGroupOperation() {
        super(SecuredSingleton.RELATIONS, ChangePermissionType.SET);
    }
}
