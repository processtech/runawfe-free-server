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
     * Gets digital signature by actor id.
     *
     * @throws DigitalSignatureDoesNotExistException
     */
    DigitalSignature getDigitalSignature(User user, Long id) throws DigitalSignatureDoesNotExistException;

    /**
     * Update digital signature.
     *
     */
    void update(User user, DigitalSignature digitalSignature);

    void updateRoot(User user, DigitalSignature digitalSignature);

    /**
     * Delete digital signature by actor id.
     */
    void remove(User user, Long id);

    void removeRootDigitalSignature(User loggedUser);

    /**
     * Whether digital signature exists.
     *
     * @param user - actor
     * @param id - actor id
     * @return
     */
    boolean doesDigitalSignatureExist(User user, Long id);

    boolean doesRootDigitalSignatureExist(User user);

    /**
     * Gets root digital signature's certificate.
     *
     * @return x509 certificate
     */
    public byte [] getRootCertificate(User user);

    DigitalSignature createRoot(User loggedUser, DigitalSignature digitalSignature);

    DigitalSignature getRootDigitalSignature(User user);


}

