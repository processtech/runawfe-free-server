package ru.runa.wfe.security.auth;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * Created on 2006
 * 
 */
public class KerberosCallbackHandler implements CallbackHandler {
    private final byte[] authToken;

    public KerberosCallbackHandler(byte[] authToken) {
        this.authToken = authToken;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof KerberosCallback) {
                KerberosCallback callback = (KerberosCallback) callbacks[i];
                callback.setAuthToken(authToken);
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback"
                        + ((callbacks[i] != null) ? " of type " + callbacks[i].getClass().getName() + " value " + callbacks[i].toString() : ""));
            }
        }
    }
}
