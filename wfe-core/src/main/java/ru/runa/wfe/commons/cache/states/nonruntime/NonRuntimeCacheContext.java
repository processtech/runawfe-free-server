package ru.runa.wfe.commons.cache.states.nonruntime;

import java.util.Calendar;
import java.util.Date;

/**
 * Context with additional data for non runtime cache state machine.
 */
public class NonRuntimeCacheContext {
    private final Date nextInitializationTime;

    public NonRuntimeCacheContext(Date nextInitializationTime) {
        super();
        this.nextInitializationTime = nextInitializationTime;
    }

    /**
     * Check if cache re initialization is required.
     *
     * @return Returns true, if cache must be re initialized and false otherwise.
     */
    public boolean isReinitializationRequired() {
        return nextInitializationTime == null || nextInitializationTime.before(Calendar.getInstance().getTime());
    }
}
