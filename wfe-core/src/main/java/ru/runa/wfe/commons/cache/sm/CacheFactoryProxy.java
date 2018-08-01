package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.sm.factories.LazyInitializedCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;

/**
 * Internal interface for hiding differences between {@link StaticCacheFactory}, {@link LazyInitializedCacheFactory} and so on.
 */
public interface CacheFactoryProxy<CacheImpl extends CacheImplementation> {
    /**
     * Create cache instance. All delayed initialization must not be started (return proxy cache object). This method called exclusive for reading
     * transactions and not exclusive for writing transactions.
     */
    CacheImpl createCache();

    /**
     * Check if delayed (lazy) cache initialization is required.
     *
     * @return Returns true, if delayed cache initialization required and false otherwise.
     */
    boolean hasDelayedInitialization();

    /**
     * Start delayed (lazy) cache initialization.
     *
     * @param context
     *            Lazy initialization context.
     */
    void startDelayedInitialization(CacheInitializationContext<CacheImpl> context);
}
