package ru.runa.wfe.security.auth;

import java.security.Principal;

import javax.security.auth.callback.Callback;

public class PrincipalCallback implements Callback {
    private Principal principal;

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }
}
