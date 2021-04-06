package ru.runa.wfe.security.auth;

import javax.security.auth.callback.Callback;

/**
 * Created on 2006
 * 
 */
public class KerberosCallback implements Callback {
    private byte[] authToken;

    public byte[] getAuthToken() {
        return authToken;
    }

    public void setAuthToken(byte[] authToken) {
        this.authToken = authToken;
    }

}
