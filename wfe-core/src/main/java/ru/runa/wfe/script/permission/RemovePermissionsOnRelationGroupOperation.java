package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = RemovePermissionsOnRelationGroupOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnRelationGroupOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnRelationGroup";

    public RemovePermissionsOnRelationGroupOperation() {
        super(SecuredSingleton.RELATIONS, ChangePermissionType.REMOVE);
    }
}
