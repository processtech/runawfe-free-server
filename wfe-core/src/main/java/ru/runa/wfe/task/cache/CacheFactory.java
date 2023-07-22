package ru.runa.wfe.task.cache;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final TaskCache TASK_CACHE = new TaskCacheStateCtrl();

    public static TaskCache getInstance() {
        return TASK_CACHE;
    }
}
