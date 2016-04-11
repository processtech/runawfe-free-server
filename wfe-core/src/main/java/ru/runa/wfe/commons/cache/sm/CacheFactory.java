package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Internal interface for hiding differences between {@link StaticCacheFactory}, {@link LazyInitializedCacheFactory} and so on.
 */
public interface CacheFactory<CacheImpl extends CacheImplementation> {
    /**
     * Create cache instance. All delayed initialization must not be started (return proxy cache object). This method called exclusive for reading
     * transactions and not exclusive for writing transactions.
     */
    public CacheImpl createCache();

    /**
     * Check if delayed (lazy) cache initialization is required.
     * 
     * @param context
     *            Lazy initialization context.
     * @return Returns true, if delayed cache initialization required and false otherwise.
     */
    public boolean hasDelayedInitialization();

    /**
     * Start delayed (lazy) cache initialization.
     * 
     * @param context
     *            Lazy initialization context.
     */
    public void startDelayedInitialization(CacheInitializationContext<CacheImpl> context);
}
