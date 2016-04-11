package ru.runa.wfe.commons.logic;

import ru.runa.wfe.security.Identifiable;

/**
 * Abstract implementation of {@link CheckMassPermissionCallback}, which do nothing for identifiables without requested permission.
 */
public abstract class IgnoreDeniedPermissionCallback implements CheckMassPermissionCallback {

    @Override
    public final void OnPermissionDenied(Identifiable identifiable) {
    }
}
