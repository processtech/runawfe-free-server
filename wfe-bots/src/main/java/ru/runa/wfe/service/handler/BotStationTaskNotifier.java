package ru.runa.wfe.service.handler;

import java.util.List;
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
    private List<BotStation> botStations;

    @Override
    public void onTaskAssigned(ParsedProcessDefinition parsedProcessDefinition, VariableProvider variableProvider, Task task, Executor previousExecutor) {
        if (SystemProperties.isAutoInvocationLocalBotStationEnabled() && task.getSwimlane() != null) {
            SwimlaneDefinition swimlaneDefinition = parsedProcessDefinition.getSwimlaneNotNull(task.getSwimlaneName());
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
