package ru.runa.wfe.security.auth;

import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

/**
 * Trusted authentication based on service user and login using internal
 * database.
 * 
 * @since 4.2.0
 */
public class TrustedLoginModule extends LoginModuleBase {

    @Override
    protected Actor login(CallbackHandler callbackHandler) throws Exception {
        if (callbackHandler instanceof TrustedLoginModuleCallbackHandler) {
            if (!SystemProperties.isTrustedAuthenticationEnabled()) {
                log.warn("trusted auth is disabled in system.properties");
                return null;
            }
            TrustedLoginModuleCallbackHandler handler = (TrustedLoginModuleCallbackHandler) callbackHandler;
            Group administratorsGroup = executorDao.getGroup(SystemProperties.getAdministratorsGroupName());
            if (!executorDao.isExecutorInGroup(handler.getServiceUser().getActor(), administratorsGroup)) {
                throw new LoginException("Service user does not belongs to administrators");
            }
            return executorDao.getActor(handler.getLogin());
        }
        return null;
    }

}
