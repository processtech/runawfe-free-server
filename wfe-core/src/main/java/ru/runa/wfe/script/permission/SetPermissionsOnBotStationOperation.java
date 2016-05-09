package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = SetPermissionsOnBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class SetPermissionsOnBotStationOperation extends ChangePermissionsOnIdentifiableOperation {

    public static final String SCRIPT_NAME = "setPermissionsOnBotStation";

    public SetPermissionsOnBotStationOperation() {
        super(BotStation.INSTANCE, ChangePermissionType.SET);
    }
}
