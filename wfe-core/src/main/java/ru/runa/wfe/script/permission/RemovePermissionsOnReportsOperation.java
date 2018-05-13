package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = RemovePermissionsOnReportsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnReportsOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnReports";

    public RemovePermissionsOnReportsOperation() {
        super(SecuredSingleton.REPORTS, ChangePermissionType.REMOVE);
    }
}
