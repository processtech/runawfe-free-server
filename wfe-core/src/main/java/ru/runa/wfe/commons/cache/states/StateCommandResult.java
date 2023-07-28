package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResult<CacheImpl extends CacheImplementation> {
    /**
     * Next state for state machine. May be null, if no state change required.
     */
    private final CacheState<CacheImpl> nextState;

    protected StateCommandResult(CacheState<CacheImpl> nextState) {
        this.nextState = nextState;
    }

    /**
     * Next state for state machine. May be null, if no state change required.
     */
    public CacheState<CacheImpl> getNextState() {
        return nextState;
    }

    /**
     * Creates result with switch to specified state.
     *
     * @param nextState
     *            State for switch to.
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation> StateCommandResult<CacheImpl> create(CacheState<CacheImpl> nextState) {
        return new StateCommandResult<>(nextState);
    }

    /**
     * Creates result without state switch.
     *
     * @return Returns command result.
     */
    public static <CacheImpl extends CacheImplementation> StateCommandResult<CacheImpl> createNoStateSwitch() {
        return create(null);
    }
}