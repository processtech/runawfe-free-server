package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.ASystem;

@XmlType(name = RemovePermissionsOnSystemOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnSystemOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnSystem";

    public RemovePermissionsOnSystemOperation() {
        super(ASystem.INSTANCE, ChangePermissionType.REMOVE);
    }
}
