package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Cache factory for static caches.
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
