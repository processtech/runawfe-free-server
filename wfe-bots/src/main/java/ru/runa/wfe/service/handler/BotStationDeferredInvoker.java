package ru.runa.wfe.service.handler;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.DeferredTransactionListener;
import ru.runa.wfe.service.delegate.BotInvokerServiceDelegate;

import com.google.common.base.Objects;
import com.google.common.base.MoreObjects;

public class BotStationDeferredInvoker extends DeferredTransactionListener {
    private final BotStation botStation;

    public BotStationDeferredInvoker(BotStation botStation) {
        this.botStation = botStation;
    }

    @Override
    public void run() {
        try {
            log.info("Invoking " + botStation);
            BotInvokerServiceDelegate.getService(botStation).invokeBots(botStation);
        } catch (Exception e) {
            log.warn("Unable to invoke " + botStation, e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BotStationDeferredInvoker) {
            return Objects.equal(botStation, ((BotStationDeferredInvoker) obj).botStation);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return botStation.hashCode();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("botStation", botStation).toString();
    }
}
