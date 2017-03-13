package ru.runa.wfe.commons.cache.sm.factories;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;

/**
 * Cache factory for creating lazily initialized caches. These caches returns some proxy implementation before initialization will be completed.
 *
 * @param <CacheImpl>
 *            Cache implementation.
 */
public interface LazyInitializedCacheFactory<CacheImpl extends CacheImplementation> {
    /**
     * Creates cache proxy. All heavy initialization must not be done.
     *
     * @return Return proxy for cache.
     */
    CacheImpl createProxy();

    /**
     * Creates fully initialized cache instance.
     *
     * @param context
     *            Cache initialization context.
     * @return Return fully initialized cache instance.
     */
    CacheImpl buildCache(CacheInitializationContext<CacheImpl> context);
}
