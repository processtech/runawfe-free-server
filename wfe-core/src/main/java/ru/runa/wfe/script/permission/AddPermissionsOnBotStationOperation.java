package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.security.SecuredSingleton;

@XmlType(name = AddPermissionsOnBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnBotStationOperation extends ChangePermissionsOnSecuredObjectOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnBotStation";

    public AddPermissionsOnBotStationOperation() {
        super(SecuredSingleton.BOTSTATIONS, ChangePermissionType.ADD);
    }
}
