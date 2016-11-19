package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Interface for interaction with cache lazy initialization process.
 * 
 * @param <CacheImpl>
 *            Cache implementation.
 */
public interface CacheInitializationContext<CacheImpl extends CacheImplementation> {

    /**
     * Check if initialization is still required. It's recommended to check this flag to stop initialization process as soon as possible. System may
     * decide to stop initialization if cache become invalid (other transaction change persistent object).
     * 
     * @return Return true, if cache initialization is required and false, if cache initialization may be stopped.
     */
    public boolean isInitializationStillRequired();

    /**
     * Must be called after cache initialization complete.
     * 
     * @param initializedCache
     *            Initialized cache instance.
     */
    public void onComplete(CacheImpl initializedCache);

    /**
     * Must be called if initialization throws exception.
     * 
     * @param e
     *            Exception, thrown during initialization.
     */
    public void onError(Throwable e);

}