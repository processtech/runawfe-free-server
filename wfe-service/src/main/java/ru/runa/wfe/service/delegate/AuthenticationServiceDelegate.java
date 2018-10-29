package ru.runa.wfe.service.delegate;

import ru.runa.wfe.service.AuthenticationService;
import ru.runa.wfe.user.User;

public class AuthenticationServiceDelegate extends Ejb3Delegate implements AuthenticationService {

    public AuthenticationServiceDelegate() {
        super(AuthenticationService.class);
    }

    private AuthenticationService getAuthenticationService() {
        return getService();
    }

    @Override
    public User authenticateByCallerPrincipal() {
        try {
            return getAuthenticationService().authenticateByCallerPrincipal();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public User authenticateByLoginPassword(String name, String password) {
        try {
            return getAuthenticationService().authenticateByLoginPassword(name, password);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public User authenticateByKerberos(byte[] token) {
        try {
            return getAuthenticationService().authenticateByKerberos(token);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public User authenticateByTrustedPrincipal(User serviceUser, String login) {
        try {
            return getAuthenticationService().authenticateByTrustedPrincipal(serviceUser, login);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
