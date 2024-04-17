package ru.runa.wfe.digitalsignature;

import ru.runa.wfe.LocalizableException;

/**
 * Signals that {@link DigitalSignature} does not exist in DB.
 */
public class DigitalSignatureDoesNotExistException extends LocalizableException {
    private static final long serialVersionUID = -1791778521016454227L;

    public DigitalSignatureDoesNotExistException(Long executorId) {
        super("error.digital_signature.id.not.exist", executorId);
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
