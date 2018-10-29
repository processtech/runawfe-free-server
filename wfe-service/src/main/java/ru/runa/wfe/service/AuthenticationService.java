package ru.runa.wfe.service;

import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.user.User;

/**
 * Service for authentication.
 * 
 * @since 2.0
 */
public interface AuthenticationService {

    /**
     * Integrated application server authentication.
     * 
     * @return authenticated user
     * @throws AuthenticationException
     */
    public User authenticateByCallerPrincipal() throws AuthenticationException;

    /**
     * Kerberos v5 authentication.
     * 
     * @param token
     *            kerberos token
     * @return authenticated user
     * @throws AuthenticationException
     */
    public User authenticateByKerberos(byte[] token) throws AuthenticationException;

    /**
     * Authentication by login and password against internal database.
     * 
     * @param login
     * @param password
     * @return authenticated user
     * @throws AuthenticationException
     */
    public User authenticateByLoginPassword(String login, String password) throws AuthenticationException;

    /**
     * Trusted authentication using service account with administrator
     * privileges. Requires setting
     * system.properties#trusted.authentication.enabled to true
     * 
     * @return authenticated user
     * @throws AuthenticationException
     * @since 4.2.0
     */
    public User authenticateByTrustedPrincipal(User serviceUser, String login) throws AuthenticationException;
}
