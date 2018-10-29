package ru.runa.wfe.security.auth;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import ru.runa.wfe.user.Actor;

/**
 * LoginModule for based on actor name and password information provided by
 * ExecutorService.
 * 
 */
public class InternalDbPasswordLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("actor name: ");
        callbacks[1] = new PasswordCallback("password: ", false);
        callbackHandler.handle(callbacks);
        String actorName = ((NameCallback) callbacks[0]).getName();
        char[] passwordChars = ((PasswordCallback) callbacks[1]).getPassword();
        if (actorName == null || passwordChars == null) {
            return null;
        }
        String password = new String(passwordChars);
        Actor actor = executorDao.getActor(actorName);
        if (executorDao.isPasswordValid(actor, password)) {
            return actor;
        }
        throw new LoginException("Invalid login or password");
    }

}
