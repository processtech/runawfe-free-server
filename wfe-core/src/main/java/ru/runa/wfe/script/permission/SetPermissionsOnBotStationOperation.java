package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = SetPermissionsOnBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOnBotStationOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "setPermissionsOnBotStation";

    public SetPermissionsOnBotStationOperation() {
        super(SecuredSingleton.BOTSTATIONS, ChangePermissionType.SET);
    }
}
