package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheState;

/**
 * Callback object to receive notification on lazy initialization complete.
 */
public interface CacheInitializationCallback<CacheImpl extends CacheImplementation> {
    /**
     * Commit builded cache.
     * 
     * @param commitedState
     *            State, which start cache building (initialization) process.
     * @param cache
     *            Builded cache to commit.
     */
    public void commitCache(CacheState<CacheImpl> commitedState, CacheImpl cache);

    /**
     * Called if cache initialization process throws exception.
     * 
     * @param commitedState
     *            State, which start cache building (initialization) process.
     * @param e
     *            Exception during initialization.
     */
    public void onError(CacheState<CacheImpl> commitedState, Throwable e);
}
