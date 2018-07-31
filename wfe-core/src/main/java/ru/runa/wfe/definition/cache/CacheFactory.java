package ru.runa.wfe.definition.cache;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final DefinitionCache DEFINITION_CACHE = new ProcessDefCacheCtrl();

    public static DefinitionCache getInstance() {
        return DEFINITION_CACHE;
    }
}
