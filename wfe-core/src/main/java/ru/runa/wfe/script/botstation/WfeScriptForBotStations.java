package ru.runa.wfe.script.botstation;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.AdminScriptRunner;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.WorkflowScriptDto;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class WfeScriptForBotStations extends AdminScriptRunner {

    private final boolean replace;
    private final BotStation botStation;

    public WfeScriptForBotStations(BotStation bs, boolean replace) {
        this.replace = replace;
        this.botStation = bs;
    }

    public static byte[] createScriptForBotLoading(Bot bot, List<BotTask> tasks) {
        Document script = XmlUtils.createDocument("workflowScript", XmlUtils.RUNA_NAMESPACE);
        Element root = script.getRootElement();
        Element createBotElement = root.addElement("createBot", XmlUtils.RUNA_NAMESPACE);
        createBotElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, bot.getUsername());
        createBotElement.addAttribute(AdminScriptConstants.PASSWORD_ATTRIBUTE_NAME, "");

        if (tasks.size() > 0) {
            Element removeTasks = root.addElement("removeConfigurationsFromBot", XmlUtils.RUNA_NAMESPACE);
            removeTasks.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, bot.getUsername());
            for (BotTask task : tasks) {
                Element taskElement = removeTasks.addElement("botConfiguration");
                taskElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, task.getName());
            }
            Element addTasks = root.addElement("addConfigurationsToBot", XmlUtils.RUNA_NAMESPACE);
            addTasks.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, bot.getUsername());
            for (BotTask task : tasks) {
                Element taskElement = addTasks.addElement("botConfiguration");
                taskElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, task.getName());
                taskElement.addAttribute(AdminScriptConstants.HANDLER_ATTRIBUTE_NAME, task.getTaskHandlerClassName());

                if (!Strings.isNullOrEmpty(task.getEmbeddedFileName())) {
                    taskElement.addAttribute(AdminScriptConstants.EMBEDDED_FILE_ATTRIBUTE_NAME, task.getEmbeddedFileName());
                }

                if (task.getConfiguration() != null && task.getConfiguration().length != 0) {
                    taskElement.addAttribute(AdminScriptConstants.CONFIGURATION_STRING_ATTRIBUTE_NAME, task.getName() + ".conf");
                }
            }
        }
        return XmlUtils.save(script);
    }

    @Override
    protected void prepareScript(WorkflowScriptDto data) {
        List<ScriptOperation> updatedOperations = Lists.newArrayList();
        for (ScriptOperation operation : data.operations) {
            if (!replace && operation instanceof RemoveConfigurationsFromBotOperation) {
                continue;
            }
            if (operation instanceof BotSystemScriptOperation) {
                ((BotSystemScriptOperation) operation).configureForBotstation(botStation);
            }
            updatedOperations.add(operation);
        }
        data.operations = updatedOperations;
    }
}
