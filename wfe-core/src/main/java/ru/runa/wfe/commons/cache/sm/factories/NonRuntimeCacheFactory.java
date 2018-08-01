package ru.runa.wfe.commons.cache.sm.factories;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;

/**
 * Cache factory for non runtime caches. It may return not actual data after data change for some time.
 *
 * @param <CacheImpl>
 *            Cache implementation.
 */
public interface NonRuntimeCacheFactory<CacheImpl extends CacheImplementation> {
    /**
     * Creates cache proxy. All heavy initialization must not be done. This instance will be returned before first initialization complete.
     *
     * @return Return proxy for cache.
     */
    CacheImpl createStub();

    /**
     * Creates fully initialized cache instance.
     *
     * @param context
     *            Cache initialization context.
     * @return Return fully initialized cache instance.
     */
    CacheImpl buildCache(CacheInitializationContext<CacheImpl> context);
}
