package ru.runa.wfe.service;

import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.digitalsignature.DigitalSignatureDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Service for operating with {@link DigitalSignature}s.
 *
 * @since 2.0
 */
public interface DigitalSignatureService {

    /**
     * Creates new digital signature.
     *
     */
    DigitalSignature create(User user, DigitalSignature digitalSignature);

    /**
     * Gets actor profile by id.
     *
     * @throws DigitalSignatureDoesNotExistException
     */
    DigitalSignature getDigitalSignature(User user, Long id) throws DigitalSignatureDoesNotExistException;

    /**
     * Update actor profile.
     *
     */
    void update(User user, DigitalSignature digitalSignature);

    /**
     * Delete actor profile by id.
     */
    void remove(User user, Long id);

    /**
     * Whether actor profile exists.
     *
     * @param user - actor
     * @param id - actor id
     * @return
     */
    boolean isDigitalSignatureExist(User user, Long id);

}

