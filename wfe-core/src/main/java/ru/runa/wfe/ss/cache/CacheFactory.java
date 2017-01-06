package ru.runa.wfe.ss.cache;

import ru.runa.wfe.commons.SystemProperties;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final SubstitutionCache SUBSTITUTION_CACHE;

    static {
        SUBSTITUTION_CACHE = SystemProperties.useCacheStateMachine() ? new SubstitutionCacheStateCtrl() : new SubstitutionCacheCtrl();
    }

    public static SubstitutionCache getInstance() {
        return SUBSTITUTION_CACHE;
    }
}
