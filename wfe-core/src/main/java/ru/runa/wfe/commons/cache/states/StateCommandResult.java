package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Command result on state machine state method call.
 */
public class StateCommandResult<CacheImpl extends CacheImplementation> {

    /**
     * Instance for no change state result.
     */
    public final static StateCommandResult stateNoChangedResult = new StateCommandResult(null);

    /**
     * Next state for state machine. May be null, if no state change required.
     */
    private final CacheState<CacheImpl> nextState;

    public StateCommandResult(CacheState<CacheImpl> nextState) {
        this.nextState = nextState;
    }

    /**
     * Next state for state machine. May be null, if no state change required.
     */
    public CacheState<CacheImpl> getNextState() {
        return nextState;
    }
}