package ru.runa.wfe.ss.cache;

import ru.runa.wfe.commons.SystemProperties;

/**
 * Factory for create cache instance. It used in system.context.xml - do not remove.
 */
final class CacheFactory {
    private static final SubstitutionCache SUBSTITUTION_CACHE;

    static {
        boolean useNonRuntime = SystemProperties.useNonRuntimeSubstitutionCache();
        SUBSTITUTION_CACHE = !SystemProperties.useCacheStateMachine() ? new SubstitutionCacheCtrl()
                : useNonRuntime ? new SubstitutionCacheStateCtrl() : new SubstitutionCacheStateCtrl(true);
    }

    public static SubstitutionCache getInstance() {
        return SUBSTITUTION_CACHE;
    }
}
