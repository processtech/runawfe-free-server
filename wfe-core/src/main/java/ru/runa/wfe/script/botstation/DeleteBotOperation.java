package ru.runa.wfe.script.botstation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

@XmlType(name = DeleteBotOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class DeleteBotOperation extends ScriptOperation implements BotSystemScriptOperation {

    public static final String SCRIPT_NAME = "deleteBot";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, required = true)
    public String botStationName;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        BotStation station = context.getBotLogic().getBotStationNotNull(botStationName);
        Bot bot = context.getBotLogic().getBotNotNull(context.getUser(), station.getId(), name);
        context.getBotLogic().removeBot(context.getUser(), bot.getId());
    }

    @Override
    public void configureForBotstation(BotStation botStation) {
        botStationName = botStation.getName();
    }
}
