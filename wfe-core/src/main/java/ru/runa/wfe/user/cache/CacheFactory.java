package ru.runa.wfe.user.cache;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final ExecutorCache EXECUTOR_CACHE = new ExecutorCacheCtrl();

    public static ExecutorCache getInstance() {
        return EXECUTOR_CACHE;
    }
}
