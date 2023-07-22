package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResultWithCache<CacheImpl extends CacheImplementation, StateContext> extends StateCommandResult<CacheImpl, StateContext> {
    /**
     * Cache instance.
     */
    private final CacheImpl cache;

    private StateCommandResultWithCache(CacheState<CacheImpl, StateContext> nextState, CacheImpl cache) {
        super(nextState);
        this.cache = cache;
    }

    /**
     * Cache instance.
     */
    public CacheImpl getCache() {
        return cache;
    }

    /**
     * Creates result with switch to specified state.
     *
     * @param nextState
     *            State for switch to.
     * @param cache
     *            Cache instance.
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation, StateContext> StateCommandResultWithCache<CacheImpl, StateContext> create(
            CacheState<CacheImpl, StateContext> nextState, CacheImpl cache) {
        return new StateCommandResultWithCache<CacheImpl, StateContext>(nextState, cache);
    }

    /**
     * Creates result without state switch.
     *
     * @param cache
     *            Cache instance.
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation, StateContext> StateCommandResultWithCache<CacheImpl, StateContext> createNoStateSwitch(
            CacheImpl cache) {
        return create(null, cache);
    }
}