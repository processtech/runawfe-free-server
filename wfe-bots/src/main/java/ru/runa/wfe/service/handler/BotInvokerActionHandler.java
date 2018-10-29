package ru.runa.wfe.service.handler;

import java.util.List;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.TransactionListeners;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Strings;

/**
 * Starts bot invocation at specified server.
 *
 * @since 2.0
 */
public class BotInvokerActionHandler extends ActionHandlerBase {

    @Override
    public void execute(ExecutionContext executionContext) {
        try {
            List<BotStation> botStations = Delegates.getBotService().getBotStations();
            BotStation botStation = null;
            if (!Strings.isNullOrEmpty(configuration)) {
                // old way: search by address
                for (BotStation bs : botStations) {
                    if (configuration.equals(bs.getAddress())) {
                        botStation = bs;
                        break;
                    }
                }
                if (botStation == null) {
                    botStation = Delegates.getBotService().getBotStationByName(configuration);
                }
            } else {
                if (botStations.size() > 0) {
                    botStation = botStations.get(0);
                }
            }
            if (botStation == null) {
                log.warn("No botstation can be found for invocation " + configuration);
                return;
            }
            TransactionListeners.addListener(new BotStationDeferredInvoker(botStation), true);
        } catch (Exception e) {
            log.error("Unable to invoke bot station due to " + e);
        }
    }

}
