package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = AddPermissionsOnSystemOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnSystemOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnSystem";

    public AddPermissionsOnSystemOperation() {
        super(SecuredSingleton.SYSTEM, ChangePermissionType.ADD);
    }
}
