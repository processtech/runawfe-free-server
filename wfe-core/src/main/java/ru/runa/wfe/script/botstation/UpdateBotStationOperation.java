package ru.runa.wfe.script.botstation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

import com.google.common.base.Strings;

@XmlType(name = UpdateBotStationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class UpdateBotStationOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "updateBotStation";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.NEW_NAME_ATTRIBUTE_NAME, required = false)
    public String newName;

    @XmlAttribute(name = AdminScriptConstants.ADDRESS_ATTRIBUTE_NAME, required = false)
    public String address;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        BotStation station = context.getBotLogic().getBotStationNotNull(name);
        if (!Strings.isNullOrEmpty(newName)) {
            station.setName(newName);
        }
        if (!Strings.isNullOrEmpty(address)) {
            station.setAddress(address);
        }
        context.getBotLogic().updateBotStation(context.getUser(), station);
    }
}
