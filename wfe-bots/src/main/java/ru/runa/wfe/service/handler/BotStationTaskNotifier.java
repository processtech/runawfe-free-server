package ru.runa.wfe.service.handler;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.logic.TaskNotifier;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.VariableProvider;

public class BotStationTaskNotifier implements TaskNotifier {

    @Override
    public void onTaskAssigned(ParsedProcessDefinition parsedProcessDefinition, VariableProvider variableProvider, Task task, Executor previousExecutor) {
        if (SystemProperties.isAutoInvocationLocalBotStationEnabled() && task.getSwimlane() != null) {
            SwimlaneDefinition swimlaneDefinition = parsedProcessDefinition.getSwimlaneNotNull(task.getSwimlaneName());
            if (swimlaneDefinition.isBotExecutor()) {
                for (BotStation botStation : Delegates.getBotService().getBotStations()) {
                    TransactionListeners.addListener(new BotStationDeferredInvoker(botStation), true);
                }
            }
        }
    }
}
