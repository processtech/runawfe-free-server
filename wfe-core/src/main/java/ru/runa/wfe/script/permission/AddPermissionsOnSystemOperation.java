package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.ASystem;

@XmlType(name = AddPermissionsOnSystemOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnSystemOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnSystem";

    public AddPermissionsOnSystemOperation() {
        super(ASystem.INSTANCE, ChangePermissionType.ADD);
    }
}
