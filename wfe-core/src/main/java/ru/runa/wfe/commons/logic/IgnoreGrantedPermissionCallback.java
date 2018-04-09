package ru.runa.wfe.commons.logic;

import ru.runa.wfe.security.SecuredObject;

/**
 * Abstract implementation of {@link CheckMassPermissionCallback}, which do nothing for secured objects with requested permission.
 */
public abstract class IgnoreGrantedPermissionCallback implements CheckMassPermissionCallback {

    @Override
    public final void OnPermissionGranted(SecuredObject securedObject) {
    }
}
