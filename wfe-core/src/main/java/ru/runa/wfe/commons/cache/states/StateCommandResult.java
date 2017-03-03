package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResult<CacheImpl extends CacheImplementation, StateContext> {
    /**
     * Next state for state machine. May be null, if no state change required.
     */
    private final CacheState<CacheImpl, StateContext> nextState;

    protected StateCommandResult(CacheState<CacheImpl, StateContext> nextState) {
        this.nextState = nextState;
    }

    /**
     * Next state for state machine. May be null, if no state change required.
     */
    public CacheState<CacheImpl, StateContext> getNextState() {
        return nextState;
    }

    /**
     * Creates result with switch to specified state.
     *
     * @param nextState
     *            State for switch to.
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation, StateContext> StateCommandResult<CacheImpl, StateContext> create(
            CacheState<CacheImpl, StateContext> nextState) {
        return new StateCommandResult<CacheImpl, StateContext>(nextState);
    }

    /**
     * Creates result without state switch.
     *
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation, StateContext> StateCommandResult<CacheImpl, StateContext> createNoStateSwitch() {
        return create(null);
    }
}