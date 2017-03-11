package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResultWithData<CacheImpl extends CacheImplementation, TData, StateContext>
        extends StateCommandResult<CacheImpl, StateContext> {

    /**
     * Some data, returned by command.
     */
    private final TData data;

    private StateCommandResultWithData(CacheState<CacheImpl, StateContext> nextState, TData data) {
        super(nextState);
        this.data = data;
    }

    /**
     * Some data, returned by command.
     */
    public TData getData() {
        return data;
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
    public static <CacheImpl extends CacheImplementation, TData, StateContext> StateCommandResultWithData<CacheImpl, TData, StateContext> create(
            CacheState<CacheImpl, StateContext> nextState, TData data) {
        return new StateCommandResultWithData<CacheImpl, TData, StateContext>(nextState, data);
    }
}