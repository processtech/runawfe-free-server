package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.TransactionalExecutor;

/**
 * Default {@link CacheTransactionalExecutor} implementation via {@link TransactionalExecutor}.
 */
public class DefaultCacheTransactionalExecutor implements CacheTransactionalExecutor {

    @Override
    public void executeInTransaction(final Runnable run) {
        new TransactionalExecutor() {

            @Override
            protected void doExecuteInTransaction() throws Exception {
                run.run();
            }
        }.executeInTransaction(true);
    }
}
