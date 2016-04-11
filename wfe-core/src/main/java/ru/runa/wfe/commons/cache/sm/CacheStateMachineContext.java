package ru.runa.wfe.commons.cache.sm;

import ru.runa.wfe.commons.cache.CacheImplementation;
import ru.runa.wfe.commons.cache.states.CacheStateFactory;

/**
 * Context for cache state machine with common used data.
 */
public class CacheStateMachineContext<CacheImpl extends CacheImplementation> {

    /**
     * Factory, used to create cache instances.
     */
    private final CacheFactory<CacheImpl> cacheFactory;

    /**
     * Callback object to receive notifications about lazy initialization complete.
     */
    private final CacheInitializationCallback<CacheImpl> callback;

    /**
     * Monitor, used for exclusive access.
     */
    private final Object monitor;

    /**
     * Transactional executor for background work (lazy caches initialization).
     */
    private final CacheTransactionalExecutor transactionalExecutor;

    /**
     * Factory to create states.
     */
    private final CacheStateFactory<CacheImpl> stateFactory;

    public CacheStateMachineContext(CacheFactory<CacheImpl> cacheFactory, CacheInitializationCallback<CacheImpl> callback,
            CacheTransactionalExecutor transactionalExecutor, Object monitor, CacheStateFactory<CacheImpl> stateFactory) {
        this.cacheFactory = cacheFactory;
        this.callback = callback;
        this.transactionalExecutor = transactionalExecutor;
        this.monitor = monitor;
        this.stateFactory = stateFactory;
    }

    /**
     * Factory, used to create cache instances.
     */
    public CacheFactory<CacheImpl> getCacheFactory() {
        return cacheFactory;
    }

    /**
     * Callback object to receive notifications about lazy initialization complete.
     */
    public CacheInitializationCallback<CacheImpl> getCallback() {
        return callback;
    }

    /**
     * Monitor, used for exclusive access.
     */
    public Object getMonitor() {
        return monitor;
    }

    /**
     * Transactional executor for background work.
     */
    public CacheTransactionalExecutor getTransactionalExecutor() {
        return transactionalExecutor;
    }

    /**
     * Factory to create states.
     */
    public CacheStateFactory<CacheImpl> getStateFactory() {
        return stateFactory;
    }
}
