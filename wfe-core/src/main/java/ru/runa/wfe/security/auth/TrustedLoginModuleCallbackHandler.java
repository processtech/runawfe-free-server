package ru.runa.wfe.security.auth;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import ru.runa.wfe.user.User;

/**
 * Callback handler for TrustedLoginModule.
 * 
 * @since 4.2.0
 */
public class TrustedLoginModuleCallbackHandler implements CallbackHandler {
    private final User serviceUser;
    private final String login;

    public TrustedLoginModuleCallbackHandler(User serviceUser, String login) {
        this.serviceUser = serviceUser;
        this.login = login;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
    }

    public User getServiceUser() {
        return serviceUser;
    }

    public String getLogin() {
        return login;
    }
}
