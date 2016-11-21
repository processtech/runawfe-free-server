package ru.runa.wfe.script.botstation;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.Identity;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidation;

import com.google.common.collect.Lists;

@XmlType(name = RemoveConfigurationsFromBotOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class RemoveConfigurationsFromBotOperation extends ScriptOperation implements BotSystemScriptOperation {

    public static final String SCRIPT_NAME = "removeConfigurationsFromBot";

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME, required = true)
    public String name;

    @XmlAttribute(name = AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, required = true)
    public String botStationName;

    @XmlElement(name = AdminScriptConstants.BOT_CONFIGURATION_ELEMENT_NAME, required = true, namespace = AdminScriptConstants.NAMESPACE)
    public List<Identity> configurations = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.NAME_ATTRIBUTE_NAME, name);
        ScriptValidation.requiredAttribute(this, AdminScriptConstants.BOTSTATION_ATTRIBUTE_NAME, botStationName);
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        BotStation botStation = context.getBotLogic().getBotStationNotNull(botStationName);
        Bot bot = context.getBotLogic().getBotNotNull(context.getUser(), botStation.getId(), name);
        for (Identity botConf : configurations) {
            BotTask botTask = context.getBotLogic().getBotTask(context.getUser(), bot.getId(), botConf.name);
            if (botTask != null) {
                context.getBotLogic().removeBotTask(context.getUser(), botTask.getId());
            }
        }
    }

    @Override
    public void configureForBotstation(BotStation botStation) {
        botStationName = botStation.getName();
    }
}
