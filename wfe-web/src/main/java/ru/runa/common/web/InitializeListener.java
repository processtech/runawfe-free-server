package ru.runa.common.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.common.Version;
import ru.runa.wf.logic.bot.BotStationResources;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.ftl.FreemarkerConfiguration;
import ru.runa.wfe.service.BotInvokerService;
import ru.runa.wfe.service.delegate.BotInvokerServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;

@CommonsLog
public class InitializeListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        Version.init();
        Delegates.getSystemService().initialize();
        FreemarkerConfiguration.forceLoadInThisClassLoader();
        log.info("initialization done in " + Thread.currentThread().getContextClassLoader());
        try {
            if (BotStationResources.isAutoStartBotStations()) {
                for (BotStation botStation : Delegates.getBotService().getBotStations()) {
                    BotInvokerService botInvokerService = BotInvokerServiceDelegate.getService(botStation);
                    if (!botInvokerService.isRunning()) {
                        botInvokerService.startPeriodicBotsInvocation(botStation);
                    }
                }
            }
        } catch (Throwable th) {
            log.error("Unable to autostart bot stations", th);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

}
