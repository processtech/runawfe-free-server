package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.report.ReportsSecure;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = SetPermissionsOnReportsOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOnReportsOperation extends ChangePermissionsOnIdentifiableOperation {

    public static final String SCRIPT_NAME = "setPermissionsOnReports";

    public SetPermissionsOnReportsOperation() {
        super(ReportsSecure.INSTANCE, ChangePermissionType.SET);
    }
}
