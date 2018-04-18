package ru.runa.wfe.script.botstation;

import com.google.common.base.Strings;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

@XmlType(name = UpdateBotOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class UpdateBotOperation extends ScriptOperation implements BotSystemScriptOperation {

    public static final String SCRIPT_NAME = "updateBot";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.NEW_NAME_ATTRIBUTE_NAME, required = false)
    public String newName;

    @XmlAttribute(name = AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, required = true)
    public String botStationName;

    @XmlAttribute(name = AdminScriptConstants.NEW_BOTSTATION_ATTRIBUTE_NAME, required = false)
    public String newBotStationName;

    @XmlAttribute(name = AdminScriptConstants.PASSWORD_ATTRIBUTE_NAME, required = false)
    public String password;

    @XmlAttribute(name = AdminScriptConstants.SEQUENTIAL_EXECUTION_ATTRIBUTE_NAME, required = false)
    public Boolean sequentialExecution;

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        BotStation station = context.getBotLogic().getBotStationNotNull(botStationName);
        Bot bot = context.getBotLogic().getBotNotNull(context.getUser(), station.getId(), name);
        if (!Strings.isNullOrEmpty(newName)) {
            bot.setUsername(newName);
        }
        if (!Strings.isNullOrEmpty(password)) {
            bot.setPassword(password);
        }
        if (!Strings.isNullOrEmpty(newBotStationName)) {
            bot.setBotStation(context.getBotLogic().getBotStationNotNull(newBotStationName));
        }
        if (sequentialExecution != null) {
            bot.setSequentialExecution(sequentialExecution);
        }
        context.getBotLogic().updateBot(context.getUser(), bot, true);
    }

    @Override
    public void configureForBotstation(BotStation botStation) {
        botStationName = botStation.getName();
    }
}
