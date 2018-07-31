package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResultWithData<CacheImpl extends CacheImplementation, TData> extends StateCommandResult<CacheImpl> {

    /**
     * Some data, returned by command.
     */
    private final TData data;

    private StateCommandResultWithData(CacheState<CacheImpl> nextState, TData data) {
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
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation, TData> StateCommandResultWithData<CacheImpl, TData> create(
            CacheState<CacheImpl> nextState, TData data
    ) {
        return new StateCommandResultWithData<>(nextState, data);
    }
}