package ru.runa.wfe.commons.logic;

import ru.runa.wfe.security.SecuredObject;

/**
 * Abstract implementation of {@link CheckMassPermissionCallback}, which do nothing for secured objects without requested permission.
 */
public abstract class IgnoreDeniedPermissionCallback implements CheckMassPermissionCallback {

    @Override
    public final void OnPermissionDenied(SecuredObject securedObject) {
    }
}
