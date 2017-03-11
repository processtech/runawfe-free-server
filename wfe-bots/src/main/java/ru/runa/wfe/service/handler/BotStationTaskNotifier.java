package ru.runa.wfe.service.handler;

import java.util.List;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SwimlaneDefinition;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.logic.ITaskNotifier;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

public class BotStationTaskNotifier implements ITaskNotifier {
    private List<BotStation> botStations;

    @Override
    public void onTaskAssigned(ProcessDefinition processDefinition, IVariableProvider variableProvider, Task task, Executor previousExecutor) {
        if (SystemProperties.isAutoInvocationLocalBotStationEnabled() && task.getSwimlane() != null) {
            SwimlaneDefinition swimlaneDefinition = processDefinition.getSwimlaneNotNull(task.getSwimlaneName());
            if (swimlaneDefinition.isBotExecutor()) {
                if (botStations == null) {
                    botStations = Delegates.getBotService().getBotStations();
                }
                for (BotStation botStation : botStations) {
                    TransactionListeners.addListener(new BotStationDeferredInvoker(botStation), true);
                }
            }
        }
    }

}
