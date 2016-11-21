package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = RemovePermissionsOnBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemovePermissionsOnBotStationOperation extends ChangePermissionsOnIdentifiableOperation {

    public static final String SCRIPT_NAME = "removePermissionsOnBotStation";

    public RemovePermissionsOnBotStationOperation() {
        super(BotStation.INSTANCE, ChangePermissionType.REMOVE);
    }
}
