package ru.runa.wfe.user.cache;

import ru.runa.wfe.commons.SystemProperties;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final ExecutorCache EXECUTOR_CACHE;

    static {
        EXECUTOR_CACHE = SystemProperties.useCacheStateMachine() ? new ExecutorCacheStateCtrl() : new ExecutorCacheCtrl();
    }

    public static ExecutorCache getInstance() {
        return EXECUTOR_CACHE;
    }
}
