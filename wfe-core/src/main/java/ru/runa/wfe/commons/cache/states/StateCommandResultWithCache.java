package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResultWithCache<CacheImpl extends CacheImplementation> extends StateCommandResult<CacheImpl> {
    /**
     * Cache instance.
     */
    private final CacheImpl cache;

    public StateCommandResultWithCache(CacheState<CacheImpl> nextState, CacheImpl cache) {
        super(nextState);
        this.cache = cache;
    }

    /**
     * Cache instance.
     */
    public CacheImpl getCache() {
        return cache;
    }
}