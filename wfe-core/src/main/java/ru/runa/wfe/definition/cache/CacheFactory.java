package ru.runa.wfe.definition.cache;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final ProcessDefCacheCtrl DEFINITION_CACHE = new ProcessDefCacheCtrl();

    public static ProcessDefCacheCtrl getInstance() {
        return DEFINITION_CACHE;
    }
}
