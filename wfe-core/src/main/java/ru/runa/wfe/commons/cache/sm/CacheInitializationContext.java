package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Interface for interaction with cache lazy initialization process.
 *
 * @param <CacheImpl>
 *            Cache implementation.
 */
public interface CacheInitializationContext<CacheImpl extends CacheImplementation> extends CacheInitializationProcessContext {

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