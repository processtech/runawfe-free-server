package ru.runa.wfe.commons.cache.common;

import ru.runa.wfe.commons.cache.sm.CacheTransactionalExecutor;

public class TestCacheTransactionalExecutor implements CacheTransactionalExecutor {

    @Override
    public void executeInTransaction(Runnable run) {
        run.run();
    }
}
