package ru.runa.wfe.commons.logic;

import ru.runa.wfe.security.Identifiable;

/**
 * Abstract implementation of {@link CheckMassPermissionCallback}, which do nothing for identifiables with requested permission.
 */
public abstract class IgnoreGrantedPermissionCallback implements CheckMassPermissionCallback {

    @Override
    public final void OnPermissionGranted(Identifiable identifiable) {
    }
}
