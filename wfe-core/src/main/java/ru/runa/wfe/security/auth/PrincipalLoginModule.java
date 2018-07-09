package ru.runa.wfe.security.auth;

import java.security.Principal;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import ru.runa.wfe.user.Actor;

public class PrincipalLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new PrincipalCallback();
        callbackHandler.handle(callbacks);
        Principal principal = ((PrincipalCallback) callbacks[0]).getPrincipal();
        if (principal == null) {
            throw new LoginException("No actor logged in.");
        }
        return executorDao.getActor(principal.getName());
    }

}
