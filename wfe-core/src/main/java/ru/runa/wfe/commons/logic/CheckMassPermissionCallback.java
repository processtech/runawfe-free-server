package ru.runa.wfe.commons.logic;

import ru.runa.wfe.security.Identifiable;

/**
 * Interface to process mass permission check results.
 */
public interface CheckMassPermissionCallback {
    /**
     * Called when identifiable has requested permission.
     * 
     * @param identifiable
     *            Identifiable, which has requested permission.
     */
    public void OnPermissionGranted(Identifiable identifiable);

    /**
     * Called when identifiable denied to requested permission.
     * 
     * @param identifiable
     *            Identifiable, which denied to requested permission.
     */
    public void OnPermissionDenied(Identifiable identifiable);
}
