package ru.runa.wfe.script.botstation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;
import ru.runa.wfe.user.Actor;

@XmlType(name = CreateBotOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class CreateBotOperation extends ScriptOperation implements BotSystemScriptOperation {

    public static final String SCRIPT_NAME = "createBot";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, required = true)
    public String botStationName;

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
        if (!context.getExecutorLogic().isExecutorExist(context.getUser(), name)) {
            Actor actor = new Actor(name, "bot");
            context.getExecutorLogic().create(context.getUser(), actor);
            context.getExecutorLogic().setPassword(context.getUser(), actor, password);
        }
        if (context.getBotLogic().getBot(context.getUser(), station.getId(), name) != null) {
            return;
        }
        Bot bot = new Bot(station, name);
        bot.setSequentialExecution(sequentialExecution);
        context.getBotLogic().createBot(context.getUser(), bot);
    }

    @Override
    public void configureForBotstation(BotStation botStation) {
        botStationName = botStation.getName();
    }
}
