package ru.runa.wfe.commons.cache.sm.factories;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Cache factory for static caches. These caches block all thread's execution before cache initialization complete.
 *
 * @param <CacheImpl>
 *            Cache implementation.
 */
public interface StaticCacheFactory<CacheImpl extends CacheImplementation> {

    /**
     * Creates fully initialized cache instance.
     *
     * @return Return fully initialized cache instance.
     */
    CacheImpl buildCache();
}
