package ru.runa.wfe.security.auth;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDAO;

public abstract class LoginModuleBase implements LoginModule {
    protected final Log log = LogFactory.getLog(getClass());
    private Subject subject;
    private CallbackHandler callbackHandler;
    private boolean commitSucceeded;
    private User user;
    private Actor actor;
    @Autowired
    protected ExecutorDAO executorDAO;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        ApplicationContextFactory.getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }

    protected abstract Actor login(CallbackHandler callbackHandler) throws Exception;

    @Override
    public boolean login() throws LoginException {
        if (callbackHandler == null) {
            throw new LoginException("No CallbackHandler provided.");
        }
        try {
            actor = login(callbackHandler);
            return actor != null;
        } catch (LoginException e) {
            throw e;
        } catch (UnsupportedCallbackException e) {
            return false;
        } catch (ExecutorDoesNotExistException e) {
            throw new LoginException(e.getMessage());
        } catch (Exception e) {
            log.error("", e);
            throw new LoginException(e.getMessage());
        }
    }

    /**
     * Method to abort the authentication process (phase 2)
     */
    @Override
    public boolean abort() {
        if (actor == null) {
            return false;
        } else if (!commitSucceeded) {
            // login succeeded but overall authentication failed
            actor = null;
        } else {
            // overall authentication succeeded and commit succeeded,
            // but someone else's commit failed
            logout();
        }
        return true;
    }

    @Override
    public boolean commit() {
        if (actor == null) {
            return false;
        }
        user = SubjectPrincipalsHelper.createUser(actor);
        if (!subject.getPrincipals().contains(user)) {
            subject.getPrincipals().add(user);
        }
        commitSucceeded = true;
        return true;

    }

    /**
     * An implementation of this method might remove/destroy a Subject's
     * Principals and Credentials.
     */
    @Override
    public boolean logout() {
        subject.getPrincipals().remove(user);
        actor = null;
        commitSucceeded = false;
        return true;
    }

}
