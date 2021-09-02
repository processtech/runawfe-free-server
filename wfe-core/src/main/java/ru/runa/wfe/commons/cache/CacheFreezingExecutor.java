package ru.runa.wfe.commons.cache;

import ru.runa.wfe.commons.cache.sm.CachingLogic;

public abstract class CacheFreezingExecutor {

    public final void execute() {
        try {
            CachingLogic.setEnabled(false);
            doExecute();
        } finally {
            CachingLogic.setEnabled(true);
            CachingLogic.resetAllCaches();
        }
    }

    protected abstract void doExecute();

}
