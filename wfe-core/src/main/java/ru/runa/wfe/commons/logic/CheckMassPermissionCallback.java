package ru.runa.wfe.commons.logic;

import ru.runa.wfe.security.SecuredObject;

/**
 * Interface to process mass permission check results.
 *
 * UPD: Redeclared as abstract class and deleted intermediate Ignore...PermissionCallback subclasses.
 */
public abstract class CheckMassPermissionCallback {

    /**
     * Called when securedObject has requested permission.
     * 
     * @param securedObject
     *            SecuredObject, which has requested permission.
     */
    public void OnPermissionGranted(SecuredObject securedObject) {
    }

    /**
     * Called when securedObject denied to requested permission.
     * 
     * @param securedObject
     *            SecuredObject, which denied to requested permission.
     */
    public void OnPermissionDenied(SecuredObject securedObject) {
    }
}
