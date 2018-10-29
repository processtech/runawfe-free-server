package ru.runa.wfe.service.delegate;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.service.BotInvokerService;

import com.google.common.base.Strings;

/**
 * Created on 04.03.2005
 */
public class BotInvokerServiceDelegate extends Ejb3Delegate implements BotInvokerService {

    public BotInvokerServiceDelegate() {
        super("BotInvokerServiceBean", BotInvokerService.class, "wfe-bots");
    }

    private BotInvokerService getBotInvokerService() {
        return getService();
    }

    @Override
    public void startPeriodicBotsInvocation(BotStation botStation) {
        getBotInvokerService().startPeriodicBotsInvocation(botStation);
    }

    @Override
    public void cancelPeriodicBotsInvocation() {
        getBotInvokerService().cancelPeriodicBotsInvocation();
    }

    @Override
    public boolean isRunning() {
        return getBotInvokerService().isRunning();
    }

    @Override
    public void invokeBots(BotStation botStation) {
        getBotInvokerService().invokeBots(botStation);
    }

    public static BotInvokerService getService(BotStation botStation) {
        BotInvokerServiceDelegate botInvokerService = Delegates.createDelegate(BotInvokerServiceDelegate.class);
        if (botStation != null && !Strings.isNullOrEmpty(botStation.getAddress())) {
            botInvokerService.setCustomProviderUrl(botStation.getAddress());
        }
        return botInvokerService;
    }

}
