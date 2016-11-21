package ru.runa.wfe.ss.cache;

import ru.runa.wfe.commons.SystemProperties;

final class CacheFactory {
    private static final SubstitutionCache SUBSTITUTION_CACHE;

    static {
        SUBSTITUTION_CACHE = SystemProperties.useCacheStateMachine() ? new SubstitutionCacheStateCtrl() : new SubstitutionCacheCtrl();
    }

    public static SubstitutionCache getInstance() {
        return SUBSTITUTION_CACHE;
    }
}
