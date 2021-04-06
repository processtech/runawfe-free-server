package ru.runa.wfe.security.auth;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Callback handler for InternalDbPasswordLoginModule. 
 * <p> Created on 19.07.2004 </p>
 */
public class PasswordLoginModuleCallbackHandler implements CallbackHandler {
    private final String name;
    private final String password;

    public PasswordLoginModuleCallbackHandler(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                NameCallback nc = (NameCallback) callbacks[i];
                nc.setName(name);
            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback pc = (PasswordCallback) callbacks[i];
                pc.setPassword(password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback"
                        + ((callbacks[i] != null) ? " of type " + callbacks[i].getClass().getName() + " value " + callbacks[i].toString() : ""));
            }
        }
    }
}
