package ru.runa.wfe.commons.logic;

/**
 * Interface to process mass permission check results.
 */
public abstract class CheckMassPermissionCallback<T> {

    /**
     * Called when "x" (SecuredObject or id) has requested permission.
     */
    public void onPermissionGranted(T x) {
    }

    /**
     * Called when "x" (SecuredObject or id) denied to requested permission.
     */
    public void onPermissionDenied(T x) {
    }
}
