package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = RemovePermissionsOnBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnBotStationOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnBotStation";

    public RemovePermissionsOnBotStationOperation() {
        super(SecuredSingleton.BOTSTATIONS, ChangePermissionType.REMOVE);
    }
}
