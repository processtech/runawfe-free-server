package ru.runa.wfe.script.permission;

import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;

@XmlType(name = AddPermissionsOnBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class AddPermissionsOnBotStationOperation extends ChangePermissionsOnIdentifiableOperation {

    public static final String SCRIPT_NAME = "addPermissionsOnBotStation";

    public AddPermissionsOnBotStationOperation() {
        super(BotStation.INSTANCE, ChangePermissionType.ADD);
    }
}
