package ru.runa.wfe.commons.cache.sm;

/**
 * Interface for transactional executors. Lazy initialized caches must be initialized in transaction at separate thread.
 */
public interface CacheTransactionalExecutor {
    /**
     * Executes action in transaction.
     * 
     * @param run
     *            Action to execute.
     */
    void executeInTransaction(Runnable run);
}
