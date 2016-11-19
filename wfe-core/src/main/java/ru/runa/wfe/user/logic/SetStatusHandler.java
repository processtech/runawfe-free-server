package ru.runa.wfe.user.logic;

import ru.runa.wfe.user.Actor;

/**
 * Interface for components, which must receive notification on actor status change.
 */
public interface SetStatusHandler {

    /**
     * Called when actor status was changed.
     * 
     * @param actor
     *            Actor, which status was changed.
     * @param isActive
     *            Actor new status.
     */
    public void onStatusChange(Actor actor, boolean isActive);
}