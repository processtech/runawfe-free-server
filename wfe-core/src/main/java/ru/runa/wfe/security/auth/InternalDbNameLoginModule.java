package ru.runa.wfe.security.auth;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.login.LoginException;
import ru.runa.wfe.user.Actor;

/**
 * LoginModule for based on actor name and password information provided by
 * ExecutorService.
 * 
 */
public class InternalDbNameLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        Callback[] callbacks = new Callback[1];
        callbacks[0] = new NameCallback("actor name: ");
        callbackHandler.handle(callbacks);
        String actorName = ((NameCallback) callbacks[0]).getName();
        if (actorName == null) {
            return null;
        }
        Actor actor = executorDao.getActor(actorName);
        if (actor != null) {
            return actor;
        }
        throw new LoginException("Invalid login or password");
    }

}
