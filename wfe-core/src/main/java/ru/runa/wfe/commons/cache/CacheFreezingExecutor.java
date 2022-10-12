package ru.runa.wfe.commons.cache;

import ru.runa.wfe.commons.cache.sm.CachingLogic;

public abstract class CacheFreezingExecutor {

    public final void execute() {
        try {
            CachingLogic.disableChangesTracking();
            doExecute();
        } finally {
            CachingLogic.enableChangesTracking();
            CachingLogic.dropAllCaches();
        }
    }

    protected abstract void doExecute();

}
