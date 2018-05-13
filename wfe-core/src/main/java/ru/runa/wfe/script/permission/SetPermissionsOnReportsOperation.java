package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = SetPermissionsOnReportsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOnReportsOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "setPermissionsOnReports";

    public SetPermissionsOnReportsOperation() {
        super(SecuredSingleton.REPORTS, ChangePermissionType.SET);
    }
}
