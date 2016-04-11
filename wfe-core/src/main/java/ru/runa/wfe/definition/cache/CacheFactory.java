package ru.runa.wfe.definition.cache;

import ru.runa.wfe.commons.SystemProperties;

final class CacheFactory {
    private static final DefinitionCache DEFINITION_CACHE;

    static {
        DEFINITION_CACHE = SystemProperties.useCacheStateMachine() ? new ProcessDefCacheStateCtrl() : new ProcessDefCacheCtrl();
    }

    public static DefinitionCache getInstance() {
        return DEFINITION_CACHE;
    }
}
