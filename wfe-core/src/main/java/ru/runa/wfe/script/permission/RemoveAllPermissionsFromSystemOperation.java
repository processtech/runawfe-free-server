package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = RemoveAllPermissionsFromSystemOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveAllPermissionsFromSystemOperation extends RemoveAllPermissionsFromSecuredObjectOperation {

    public static final String SCRIPT_NAME = "removeAllPermissionsFromSystem";

    public RemoveAllPermissionsFromSystemOperation() {
        super(SCRIPT_NAME, SecuredSingleton.SYSTEM);
    }
}
