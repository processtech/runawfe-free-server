package ru.runa.wfe.security.auth;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;

import ru.runa.wfe.commons.ftl.ExpressionEvaluator;
import ru.runa.wfe.security.logic.LdapProperties;
import ru.runa.wfe.user.Actor;

import com.google.common.collect.Maps;

/**
 * MS Active Directory based login module.
 * 
 * @since 2.0
 */
public class LdapLoginModule extends LoginModuleBase {
    private Hashtable<String, String> env = new Hashtable<String, String>();

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        super.initialize(subject, callbackHandler, sharedState, options);
        env.putAll(LdapProperties.getAllProperties());
    }

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("actor name: ");
        callbacks[1] = new PasswordCallback("password: ", false);
        callbackHandler.handle(callbacks);
        String actorName = ((NameCallback) callbacks[0]).getName();
        if (actorName == null) {
            throw new LoginException("No actor name was provided.");
        }
        char[] tmpPasswordChars = ((PasswordCallback) callbacks[1]).getPassword();
        if (tmpPasswordChars == null || tmpPasswordChars.length == 0) {
            throw new LoginException("No password was provided.");
        }
        String password = new String(tmpPasswordChars);
        env.put(Context.SECURITY_PRINCIPAL, formatUsername(actorName));
        env.put(Context.SECURITY_CREDENTIALS, password);
        try {
            DirContext ctx = new InitialDirContext(env);
            ctx.close();
        } catch (AuthenticationException e) {
            log.warn(e);
            throw new LoginException("invalid login or password");
        }
        return executorDao.getActorCaseInsensitive(actorName);
    }

    private String formatUsername(String username) {
        Map<String, String> variables = Maps.newHashMap();
        variables.put("username", username);
        return ExpressionEvaluator.substitute(LdapProperties.getAuthenticationUsernameFormat(), variables);
    }

}
