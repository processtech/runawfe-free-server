package ru.runa.wfe.security.auth;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

public class PrincipalCallbackHandler implements CallbackHandler {

    private final Principal principal;

    public PrincipalCallbackHandler(Principal principal) {
        this.principal = principal;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof PrincipalCallback) {
                PrincipalCallback callback = (PrincipalCallback) callbacks[i];
                callback.setPrincipal(principal);
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback"
                        + ((callbacks[i] != null) ? " of type " + callbacks[i].getClass().getName() + " value " + callbacks[i].toString() : ""));
            }
        }
    }
}
