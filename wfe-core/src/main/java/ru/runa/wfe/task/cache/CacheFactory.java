package ru.runa.wfe.task.cache;

import ru.runa.wfe.commons.SystemProperties;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final TaskCache TASK_CACHE;

    static {
        TASK_CACHE = SystemProperties.useCacheStateMachine() ? new TaskCacheStateCtrl() : new TaskCacheCtrl();
    }

    public static TaskCache getInstance() {
        return TASK_CACHE;
    }
}
